package com.mambobryan.repositories

import com.mambobryan.data.models.ServerResponse
import com.mambobryan.data.query
import com.mambobryan.data.requests.PoemRequest
import com.mambobryan.data.tables.poem.Poem
import com.mambobryan.data.tables.poem.PoemsTable
import com.mambobryan.data.tables.poem.toPoem
import com.mambobryan.utils.defaultBadRequestResponse
import com.mambobryan.utils.defaultOkResponse
import com.mambobryan.utils.serverErrorResponse
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

    suspend fun getPoems(userId: UUID) = query {

        return@query try {

            val poems = PoemsTable.selectAll().sortedByDescending { PoemsTable.updatedAt }.map { it.toPoem() }

            defaultOkResponse(
                message = "success", data = poems
            )

        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)
        }

    }

    suspend fun getPoems(userId: UUID, query: String) = query {

        return@query try {

            val condition = Op.build { (PoemsTable.title.lowerCase() like "%$query%".lowercase() or (PoemsTable.content.lowerCase() like "%$query%".lowercase())) }

            val poems = PoemsTable.select { condition }.sortedByDescending { PoemsTable.updatedAt }.map { it.toPoem() }

            defaultOkResponse(message = "success", data = poems)

        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)
        }

    }

    suspend fun getPoems(userId: UUID, topic: Int) = query {

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


}