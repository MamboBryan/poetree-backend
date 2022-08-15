package com.mambobryan.repositories

import com.mambobryan.data.models.ServerResponse
import com.mambobryan.data.query
import com.mambobryan.data.tables.comment.CommentLikesTable
import com.mambobryan.data.tables.comment.CommentsTable
import com.mambobryan.data.tables.poem.PoemLikesTable
import com.mambobryan.data.tables.poem.PoemsTable
import com.mambobryan.utils.defaultCreatedResponse
import com.mambobryan.utils.defaultNotFoundResponse
import com.mambobryan.utils.defaultOkResponse
import com.mambobryan.utils.serverErrorResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.time.LocalDateTime
import java.util.*

class LikeRepository {

    suspend fun likePoem(userId: UUID, poemId: UUID): ServerResponse<out Any?> {

        return try {

            query { PoemsTable.select(PoemsTable.id eq poemId).firstOrNull() }
                ?: return defaultNotFoundResponse(message = "poem not found")

            val liked = query {
                PoemLikesTable.select { PoemLikesTable.userId eq userId and (PoemLikesTable.poemId eq poemId) }
                    .firstOrNull()
            }
            if (liked != null) return defaultOkResponse(message = "poem already liked", data = null)

            query {
                PoemLikesTable.insert {
                    it[PoemLikesTable.createdAt] = LocalDateTime.now()
                    it[PoemLikesTable.userId] = userId
                    it[PoemLikesTable.poemId] = poemId
                }
            }

            defaultCreatedResponse(message = "poem liked", data = null)

        } catch (e: Exception) {

            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)

        }

    }

    suspend fun unlikePoem(userId: UUID, poemId: UUID) = query {
        try {

            PoemsTable.select(PoemsTable.id eq poemId).firstOrNull()
                ?: return@query defaultNotFoundResponse(message = "poem not found")

            val result =
                PoemLikesTable.deleteWhere { PoemLikesTable.userId eq userId and (PoemLikesTable.poemId eq poemId) }

            defaultOkResponse(message = "poem unliked", data = result != 0)

        } catch (e: Exception) {

            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)

        }
    }

    suspend fun likeComment(userId: UUID, commentId: UUID): ServerResponse<out Any?> {

        return try {

            query { CommentsTable.select(CommentsTable.id eq commentId).firstOrNull() }
                ?: return defaultNotFoundResponse(message = "comment not found")

            val liked = query {
                CommentLikesTable.select { CommentLikesTable.userId eq userId and (CommentLikesTable.commentId eq commentId) }
                    .firstOrNull()
            }
            if (liked != null) return defaultOkResponse(message = "comment already liked", data = null)

            query {
                CommentLikesTable.insert {
                    it[CommentLikesTable.createdAt] = LocalDateTime.now()
                    it[CommentLikesTable.userId] = userId
                    it[CommentLikesTable.commentId] = commentId
                }
            }

            defaultCreatedResponse(message = "poem liked", data = null)

        } catch (e: Exception) {

            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)

        }

    }

    suspend fun unlikeComment(userId: UUID, commentId: UUID) = query {
        try {

            CommentsTable.select(CommentsTable.id eq commentId).firstOrNull()
                ?: return@query defaultNotFoundResponse(message = "comment not found")

            val result = CommentLikesTable.deleteWhere {
                CommentLikesTable.userId eq userId and (CommentLikesTable.commentId eq commentId)
            }

            defaultOkResponse(message = "comment unliked", data = result != 0)

        } catch (e: Exception) {

            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)

        }
    }


}