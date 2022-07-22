package com.mambobryan.repositories

import com.mambobryan.data.models.ServerResponse
import com.mambobryan.data.query
import com.mambobryan.data.requests.PoemRequest
import com.mambobryan.data.tables.comment.CommentsTable
import com.mambobryan.data.tables.poem.*
import com.mambobryan.data.tables.poem.relations.CompletePoemEntity
import com.mambobryan.data.tables.poem.relations.toPoemDto
import com.mambobryan.data.tables.poem.toBookmark
import com.mambobryan.data.tables.poem.toPoem
import com.mambobryan.data.tables.topic.TopicsTable
import com.mambobryan.data.tables.user.UsersTable
import com.mambobryan.utils.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.time.LocalDateTime
import java.util.*

class PoemsRepository {

    suspend fun create(userId: UUID, request: PoemRequest): ServerResponse<out Any?> {

        if (request.title.isNullOrBlank()) return defaultBadRequestResponse("Invalid Title")
        if (request.content.isNullOrBlank()) return defaultBadRequestResponse("Invalid Content")
        if (request.html.isNullOrBlank()) return defaultBadRequestResponse("Invalid Html content")
        if (request.topic == null) return defaultBadRequestResponse("Invalid Topic Id")

        val now = LocalDateTime.now()

        var statement: InsertStatement<Number>? = null

        return try {

            query {
                statement = PoemsTable.insert {
                    it[PoemsTable.createdAt] = now
                    it[PoemsTable.updatedAt] = now
                    it[PoemsTable.title] = request.title
                    it[PoemsTable.content] = request.content
                    it[PoemsTable.contentAsHtml] = request.html
                    it[PoemsTable.userId] = userId
                    it[PoemsTable.topicId] = request.topic
                }
            }

            defaultOkResponse(message = "poem created", data = statement?.resultedValues?.get(0).toPoem())

        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)
        }

    }

    suspend fun update(userId: UUID, poemId: UUID, request: PoemRequest): ServerResponse<out Any?> = query {

        if (request.title.isNullOrBlank() && request.content.isNullOrBlank() && request.html.isNullOrBlank() && request.topic == null) return@query defaultBadRequestResponse(
            "Invalid title, content, html or topic"
        )

        val condition = Op.build { PoemsTable.id eq poemId and (PoemsTable.userId eq userId) }

        PoemsTable.select(condition).singleOrNull() ?: defaultBadRequestResponse("This poem is not yours!")

        return@query try {

            PoemsTable.update({ condition }) {
                it[PoemsTable.updatedAt] = LocalDateTime.now()
                it[PoemsTable.editedAt] = LocalDateTime.now()
                if (!request.title.isNullOrBlank()) it[PoemsTable.title] = request.title
                if (!request.content.isNullOrBlank()) it[PoemsTable.content] = request.content
                if (!request.html.isNullOrBlank()) it[PoemsTable.contentAsHtml] = request.html
                if (request.topic != null) it[PoemsTable.topicId] = request.topic
            }

            val updatedPoem = PoemsTable.select { condition }.firstOrNull().toPoem()

            defaultOkResponse(message = "poem updated", data = updatedPoem)

        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)
        }

    }

    suspend fun delete(userId: UUID, poemId: UUID) = query {

        return@query try {

            val result = PoemsTable.deleteWhere { PoemsTable.id eq poemId and (PoemsTable.userId eq userId) }

            when (result != 0) {
                true -> defaultOkResponse(message = "poem deleted", data = true)
                false -> serverErrorResponse(message = "unable to delete poem")
            }

        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)
        }

    }

    suspend fun getPoem(userId: UUID, poemId: UUID) = query {

        return@query try {

            val poem = PoemsTable.select(PoemsTable.id eq poemId).map { it.toPoem() }.singleOrNull()

            defaultOkResponse(message = "success", data = poem)

        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)
        }

    }

    suspend fun getPoems(userId: UUID, page: Int = 1) = query {

        val (limit, offset) = getLimitAndOffset(page)

        return@query try {

            val readCondition = Op.build { ReadsTable.poemId eq PoemsTable.id }
            val reads = ReadsTable.id.count()
            val read =
                exists(ReadsTable.select { ReadsTable.poemId eq PoemsTable.id and (ReadsTable.userId eq userId) })

            val like = Op.build { PoemLikesTable.poemId eq PoemsTable.id }
            val likes = PoemLikesTable.id.count()
            val liked =
                exists(PoemLikesTable.select { PoemLikesTable.poemId eq PoemsTable.id and (PoemLikesTable.userId eq userId) })

            val bookmark = Op.build { BookmarksTable.poemId eq PoemsTable.id }
            val bookmarks = BookmarksTable.id.count()
            val bookmarked =
                exists(BookmarksTable.select { BookmarksTable.poemId eq PoemsTable.id and (BookmarksTable.userId eq userId) })

            val comment = Op.build { CommentsTable.poemId eq PoemsTable.id }
            val comments = CommentsTable.id.count()
            val commented =
                exists(CommentsTable.select { CommentsTable.poemId eq PoemsTable.id and (CommentsTable.userId eq userId) })

            val columns = listOf(
                reads, read,
                bookmarks, bookmarked,
                likes, liked,
                comments, commented
            )

            val list = PoemsTable.innerJoin(UsersTable)
                .innerJoin(TopicsTable)
                .join(otherTable = ReadsTable, joinType = JoinType.LEFT, additionalConstraint = { readCondition })
                .join(otherTable = BookmarksTable, joinType = JoinType.LEFT, additionalConstraint = { bookmark })
                .join(otherTable = PoemLikesTable, joinType = JoinType.LEFT, additionalConstraint = { like })
                .join(otherTable = CommentsTable, joinType = JoinType.LEFT, additionalConstraint = { comment })
                .slice(columns)
                .selectAll()
                .groupBy(PoemsTable.id, UsersTable.id, TopicsTable.id)
                .orderBy(PoemsTable.createdAt, SortOrder.DESC)
                .limit(n = limit, offset = offset)
                .map {
                    mapOf(
//                        "poem" to it[PoemsTable.title],
//                        "topic" to it[TopicsTable.name],
//                        "user" to it[UsersTable.userName],
                        "liked" to it[liked],
                        "likes" to it[likes],
                        "bookmarked" to it[bookmarked],
                        "bookmarks" to it[bookmarks],
                        "read" to it[read],
                        "reads" to it[reads],
                        "commented" to it[commented],
                        "comments" to it[comments],
                    )
                }

            defaultOkResponse(message = "success", data = getPagedData(page = page, result = list))

        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = e.message.toString())
        }

    }

    suspend fun getPoems(userId: UUID, query: String, page: Int = 1) = query {

        val (limit, offset) = getLimitAndOffset(page)

        return@query try {

            val condition =
                Op.build { (PoemsTable.title.lowerCase() like "%$query%".lowercase() or (PoemsTable.content.lowerCase() like "%$query%".lowercase())) }

            val poems = PoemsTable.select { condition }.limit(n = limit, offset = offset)
                .sortedByDescending { PoemsTable.updatedAt }.map { it.toPoem() }

            defaultOkResponse(message = "success", data = poems)

        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)
        }

    }

    suspend fun getPoems(userId: UUID, topic: Int, page: Int = 1) = query {

        return@query try {

            val condition = Op.build { PoemsTable.topicId eq topic }

            val poems = PoemsTable.select { condition }.sortedByDescending { PoemsTable.updatedAt }.map { it.toPoem() }

            defaultOkResponse(message = "success", data = poems)

        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)
        }

    }

    suspend fun getPoems(userId: UUID, topic: Int, query: String) = query {

        return@query try {

            val condition =
                Op.build { PoemsTable.topicId eq topic and (PoemsTable.title like "%$query%" or (PoemsTable.content like "%$query%")) }

            val poems = PoemsTable.select { condition }.sortedByDescending { PoemsTable.updatedAt }.map { it.toPoem() }

            defaultOkResponse(message = "success", data = poems)

        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)
        }

    }

    suspend fun markAsRead(userId: UUID, poemId: UUID): ServerResponse<out Any?> {

        var statement: InsertStatement<Number>? = null

        return try {

            val exists = ReadsTable.select { ReadsTable.userId eq userId and (ReadsTable.poemId eq poemId) }
                .firstOrNull() != null

            if (exists) return defaultOkResponse(message = "poem already read", data = null)

            query {
                statement = ReadsTable.insert {
                    it[ReadsTable.createdAt] = LocalDateTime.now()
                    it[ReadsTable.userId] = userId
                    it[ReadsTable.poemId] = poemId
                }
            }

            defaultOkResponse(message = "read", data = statement?.resultedValues?.get(0).toBookmark())

        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)
        }

    }


}