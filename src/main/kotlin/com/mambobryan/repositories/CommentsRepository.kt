package com.mambobryan.repositories

import com.mambobryan.data.models.ServerResponse
import com.mambobryan.data.query
import com.mambobryan.data.requests.CommentRequest
import com.mambobryan.data.tables.comment.*
import com.mambobryan.data.tables.comment.toComment
import com.mambobryan.data.tables.comment.toCommentDto
import com.mambobryan.data.tables.poem.*
import com.mambobryan.data.tables.user.UsersTable
import com.mambobryan.utils.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.time.LocalDateTime
import java.util.*

class CommentsRepository {

    suspend fun create(userID: UUID, poemId: UUID, request: CommentRequest): ServerResponse<out Any?> {

        return try {

            if (request.content.isNullOrBlank()) return defaultBadRequestResponse("comment content cannot be blank")

            query { PoemsTable.select(PoemsTable.id eq poemId).firstOrNull() }
                ?: return defaultNotFoundResponse(message = "poem not found")

            var statement: InsertStatement<Number>? = null

            query {
                statement = CommentsTable.insert {
                    it[CommentsTable.createdAt] = LocalDateTime.now()
                    it[CommentsTable.userId] = userID
                    it[CommentsTable.poemId] = poemId
                    it[CommentsTable.content] = request.content
                }
            }

            val data = statement?.resultedValues?.get(0).toComment().toCommentDto()

            return defaultOkResponse(message = "comment created", data = data)

        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)
        }

    }

    suspend fun getComment(userId: UUID, commentId: UUID) = query {

        try {

            val data = CommentsTable.select { CommentsTable.id eq commentId }.map { it.toComment().toCommentDto() }
                .firstOrNull()
            defaultOkResponse(message = "comment", data = data)

        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)
        }

    }

    private fun getLikesData(userId: UUID): Pair<exists, Count> {
        val likes = CommentLikesTable.id.count()
        val liked =
            exists(CommentLikesTable.select { CommentLikesTable.commentId eq CommentsTable.id and (CommentLikesTable.userId eq userId) })
        return Pair(liked, likes)
    }

    private fun getQuery(userId: UUID): FieldSet {

        val like = Op.build { CommentLikesTable.commentId eq CommentsTable.id }
        val (liked, likes) = getLikesData(userId)

        val poemCondition = Op.build { CommentsTable.poemId eq PoemsTable.id }

        val columns = listOf(
            likes,
            liked,
            *CommentsTable.columns.toTypedArray(),
            *UsersTable.columns.toTypedArray(),
            *PoemsTable.columns.toTypedArray(),
        )

        return CommentsTable.innerJoin(UsersTable)
            .join(otherTable = PoemsTable, joinType = JoinType.LEFT, additionalConstraint = { poemCondition })
            .join(otherTable = CommentLikesTable, joinType = JoinType.LEFT, additionalConstraint = { like })
            .slice(columns)

    }

    suspend fun getComments(userId: UUID, poemId: UUID, page: Int) = query {

        val (limit, offset) = getLimitAndOffset(page)

        return@query try {

            val query = getQuery(userId = userId)
            val (liked, likes) = getLikesData(userId)

            val selectCondition = Op.build { CommentsTable.poemId eq poemId }

            val data = query.select(selectCondition).groupBy(CommentsTable.id, UsersTable.id, PoemsTable.id)
                .orderBy(CommentsTable.createdAt, SortOrder.DESC).limit(n = limit, offset = offset).map {
                    it.toCompleteCommentDto(
                        liked = it[liked], likes = it[likes]
                    )
                }

            defaultOkResponse(message = "success", data = getPagedData(page = page, result = data))

        } catch (e: Exception) {

            println(e.localizedMessage)
            serverErrorResponse(message = e.message.toString())

        }

    }

    suspend fun getPoemComments(userId: UUID, poemId: UUID) = query {
        val list =
            CommentsTable.select { CommentsTable.poemId eq poemId }.sortedByDescending { CommentsTable.updatedAt }
                .map { it.toComment() }
        defaultOkResponse(message = "comments", data = list)
    }

    suspend fun update(userId: UUID, request: CommentRequest): ServerResponse<out Any?> {

        val commentId = request.commentId.asUUID() ?: return defaultBadRequestResponse(message = "Invalid comment id")

        if (request.content.isNullOrBlank()) return defaultBadRequestResponse(message = "Invalid comment content")

        val result = query {
            try {

                val condition = Op.build { CommentsTable.id eq commentId and (CommentsTable.userId eq userId) }
                CommentsTable.select(condition).singleOrNull()
                    ?: return@query defaultBadRequestResponse("This comment is not yours!")

                CommentsTable.update({ condition }) {
                    it[CommentsTable.updatedAt] = LocalDateTime.now()
                    it[CommentsTable.content] = request.content
                }

                null

            } catch (e: Exception) {
                println(e.localizedMessage)
                serverErrorResponse(message = e.localizedMessage)
            }
        }

        if (result != null) return result

        return getComment(userId = userId, commentId = commentId)

    }

    suspend fun delete(userId: UUID, commentId: UUID) = query {
        return@query try {

            CommentsTable.select(CommentsTable.id eq commentId).firstOrNull() ?: return@query defaultNotFoundResponse(
                message = "comment not found"
            )

            val result =
                CommentsTable.deleteWhere { CommentsTable.id eq commentId and (CommentsTable.userId eq userId) }

            defaultOkResponse(message = "comment deleted", data = result != 0)

        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)
        }
    }

}