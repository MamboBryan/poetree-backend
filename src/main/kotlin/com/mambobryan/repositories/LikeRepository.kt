package com.mambobryan.repositories

import com.mambobryan.data.models.ServerResponse
import com.mambobryan.data.query
import com.mambobryan.data.tables.comment.CommentLikesTable
import com.mambobryan.data.tables.comment.toCommentLike
import com.mambobryan.data.tables.poem.PoemLikesTable
import com.mambobryan.data.tables.poem.toPoemLike
import com.mambobryan.utils.defaultOkResponse
import com.mambobryan.utils.serverErrorResponse
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.time.LocalDateTime
import java.util.*

class LikeRepository {

    suspend fun likePoem(userId: UUID, poemId: UUID): ServerResponse<out Any?> {

        var statement: InsertStatement<Number>? = null
        var exists = false

        return try {

            query {

                exists = PoemLikesTable.select { PoemLikesTable.userId eq userId and (PoemLikesTable.poemId eq poemId) }
                    .firstOrNull() != null

                if (exists) return@query

                statement = PoemLikesTable.insert {
                    it[PoemLikesTable.createdAt] = LocalDateTime.now()
                    it[PoemLikesTable.userId] = userId
                    it[PoemLikesTable.poemId] = poemId
                }
            }

            when (exists) {
                true -> defaultOkResponse(message = "Poem already liked", data = null)
                false -> defaultOkResponse(
                    message = "poem liked", data = statement?.resultedValues?.get(0).toPoemLike()
                )
            }


        } catch (e: Exception) {

            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)

        }

    }

    suspend fun unlikePoem(userId: UUID, poemId: UUID) = query {
        try {

            val result =
                PoemLikesTable.deleteWhere { PoemLikesTable.userId eq userId and (PoemLikesTable.poemId eq poemId) }
            defaultOkResponse(message = "poem unliked", data = result != 0)

        } catch (e: Exception) {

            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)

        }
    }

    suspend fun likeComment(userId: UUID, commentId: UUID): ServerResponse<out Any?> {

        var statement: InsertStatement<Number>? = null
        var exists = false

        return try {

            query {

                exists =
                    CommentLikesTable.select { CommentLikesTable.userId eq userId and (CommentLikesTable.commentId eq commentId) }
                        .firstOrNull() != null

                if (exists) return@query

                statement = CommentLikesTable.insert {
                    it[CommentLikesTable.createdAt] = LocalDateTime.now()
                    it[CommentLikesTable.userId] = userId
                    it[CommentLikesTable.commentId] = commentId
                }
            }

            when (exists) {
                true -> defaultOkResponse(message = "Comment already liked", data = null)
                false -> defaultOkResponse(
                    message = "comment liked", data = statement?.resultedValues?.get(0).toCommentLike()
                )

            }

        } catch (e: Exception) {

            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)

        }

    }

    suspend fun unlikeComment(userId: UUID, poemId: UUID) = query {
        try {

            val result =
                CommentLikesTable.deleteWhere { CommentLikesTable.userId eq userId and (CommentLikesTable.commentId eq poemId) }
            defaultOkResponse(message = "comment unliked", data = result != 0)

        } catch (e: Exception) {

            println(e.localizedMessage)
            serverErrorResponse(message = e.localizedMessage)

        }
    }


}