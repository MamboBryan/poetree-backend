package com.mambobryan.repositories

import com.mambobryan.data.models.ServerResponse
import com.mambobryan.data.query
import com.mambobryan.data.requests.CommentRequest
import com.mambobryan.data.tables.comment.CommentsTable
import com.mambobryan.data.tables.comment.toComment
import com.mambobryan.utils.defaultBadRequestResponse
import com.mambobryan.utils.defaultOkResponse
import com.mambobryan.utils.serverErrorResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.time.LocalDateTime
import java.util.*

class CommentsRepository {

    suspend fun create(userID: UUID, poemId: UUID, request: CommentRequest): ServerResponse<Any?> {

        if (request.content.isNullOrBlank()) return defaultBadRequestResponse("invalid message")

        var statement: InsertStatement<Number>? = null

        query {
            statement = CommentsTable.insert {
                it[CommentsTable.createdAt] = LocalDateTime.now()
                it[CommentsTable.userId] = userID
                it[CommentsTable.poemId] = poemId
                it[CommentsTable.content] = request.content
            }
        }

        return defaultOkResponse(
            message = "comment created", data = statement?.resultedValues?.get(0).toComment()
        )

    }

    suspend fun get(userId: UUID, commentId: UUID) = query {

        val topic = CommentsTable.select { CommentsTable.id eq commentId }.map { it.toComment() }.firstOrNull()
        defaultOkResponse(message = "comment", data = topic)

    }

    suspend fun getComments(userId: UUID, poemId: UUID) = query {
        val list = CommentsTable.selectAll().sortedByDescending { CommentsTable.updatedAt }.map { it.toComment() }
        defaultOkResponse(message = "comments", data = list)
    }

    suspend fun getPoemComments(userId: UUID, poemId: UUID) = query {
        val list = CommentsTable.select { CommentsTable.poemId eq poemId }
            .sortedByDescending { CommentsTable.updatedAt }
            .map { it.toComment() }
        defaultOkResponse(message = "comments", data = list)
    }

    suspend fun update(userId: UUID, commentId: UUID, request: CommentRequest) = query {

        if (request.content.isNullOrBlank()) return@query defaultBadRequestResponse("Invalid comment content")

        val condition = Op.build { CommentsTable.id eq commentId and (CommentsTable.userId eq userId) }
        CommentsTable.select(condition).singleOrNull() ?: defaultBadRequestResponse("This comment is not yours!")

        try {

            CommentsTable.update({ condition }) {
                it[CommentsTable.updatedAt] = LocalDateTime.now()
                it[CommentsTable.content] = request.content

            }

            val comment = CommentsTable.select(CommentsTable.id eq commentId).map { it.toComment() }.singleOrNull()
            defaultOkResponse(message = "comment updated", data = comment)

        } catch (e:Exception){
            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)
        }

    }

    suspend fun delete(userId: UUID, commentId: UUID) = query {
        return@query try {

            val result = CommentsTable.deleteWhere { CommentsTable.id eq commentId and (CommentsTable.userId eq userId) }

            when (result != 0) {
                true -> defaultOkResponse(message = "poem deleted", data = true)
                false -> serverErrorResponse(message = "unable to delete poem")
            }

        } catch (e: Exception) {
            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)
        }
    }

}