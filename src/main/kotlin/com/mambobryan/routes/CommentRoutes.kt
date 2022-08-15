package com.mambobryan.routes

import com.mambobryan.data.requests.CommentRequest
import com.mambobryan.repositories.CommentsRepository
import com.mambobryan.repositories.LikeRepository
import com.mambobryan.utils.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.commentRoutes() {

    val commentRepository = CommentsRepository()
    val likeRepository = LikeRepository()

    route("comments") {

        // create comment
        post {
            val currentUserId = call.getCurrentUserId() ?: return@post call.defaultResponse(
                status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
            )

            val request = call.receive<CommentRequest>()

            val poemId = request.poemId.asUUID() ?: return@post call.defaultResponse(
                status = HttpStatusCode.BadRequest, message = "Invalid poem id"
            )

            val response = commentRepository.create(userID = currentUserId, poemId = poemId, request = request)
            call.respond(response)
        }

        route("comment"){

            // get comment
            post {
                val currentUserId = call.getCurrentUserId() ?: return@post call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val request = call.receive<CommentRequest>()

                val commentId = request.commentId.asUUID() ?: return@post call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid comment id"
                )

                val response = commentRepository.getComment(userId = currentUserId, commentId = commentId)
                call.respond(response)
            }

            // update comment
            put {
                val currentUserId = call.getCurrentUserId() ?: return@put call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val request = call.receive<CommentRequest>()

                val response = commentRepository.update(userId = currentUserId, request = request)
                call.respond(response)
            }

            // delete comment
            delete {

                val currentUserId = call.getCurrentUserId() ?: return@delete call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val request = call.receive<CommentRequest>()

                val commentId = request.commentId.asUUID() ?: return@delete call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid comment id"
                )
                val response = commentRepository.delete(userId = currentUserId, commentId = commentId)
                call.respond(response)
            }

            // like comment
            post("like") {
                val currentUserId = call.getCurrentUserId() ?: return@post call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val request = call.receive<CommentRequest>()

                val commentId = request.commentId.asUUID() ?: return@post call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid comment id"
                )

                val response = likeRepository.likeComment(userId = currentUserId, commentId = commentId)
                call.respond(response)
            }

            // unlike comment
            delete("unlike") {

                val currentUserId = call.getCurrentUserId() ?: return@delete call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val request = call.receive<CommentRequest>()

                val commentId = request.commentId.asUUID() ?: return@delete call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid comment id"
                )
                val response = likeRepository.unlikeComment(userId = currentUserId, commentId = commentId)
                call.respond(response)
            }

        }

    }

}