package com.mambobryan.routes

import com.mambobryan.data.requests.CommentRequest
import com.mambobryan.data.requests.PoemRequest
import com.mambobryan.repositories.BookmarkRepository
import com.mambobryan.repositories.CommentsRepository
import com.mambobryan.repositories.LikeRepository
import com.mambobryan.repositories.PoemsRepository
import com.mambobryan.utils.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.poemRoutes() {

    val poemsRepository = PoemsRepository()
    val bookmarkRepository = BookmarkRepository()
    val likeRepository = LikeRepository()
    val commentRepository = CommentsRepository()

    route("poems") {

        post {

            val currentUserId = call.getCurrentUserId() ?: return@post call.defaultResponse(
                status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
            )

            val request = call.receive<PoemRequest>()

            val response = poemsRepository.create(userId = currentUserId, request = request)

            call.respond(response)

        }

        get {

            val currentUserId = call.getCurrentUserId() ?: return@get call.defaultResponse(
                status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
            )

            val query = call.getQuery("q")

            val topic = call.getQuery("topic")

            val page = call.getQuery("page")?.toIntOrNull() ?: 1

            if (topic.isNullOrBlank().not()) {
                if (topic?.toIntOrNull() == null) return@get call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid topic id"
                )
            }

            val response = poemsRepository.getPoems(
                userId = currentUserId,
                topic = topic?.toIntOrNull(),
                queryString = query,
                page = page
            )

            call.respond(response)

        }

        route("{id}") {

            get {

                val currentUserId = call.getCurrentUserId() ?: return@get call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val poemId = call.getUrlParameter("id").asUUID() ?: return@get call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid Id"
                )

                val response = poemsRepository.getPoem(userId = currentUserId, poemId = poemId)
                call.respond(response)

            }

            put {
                val currentUserId = call.getCurrentUserId() ?: return@put call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val poemId = call.getUrlParameter("id").asUUID() ?: return@put call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid Id"
                )

                val request = call.receive<PoemRequest>()

                val response = poemsRepository.update(userId = currentUserId, poemId = poemId, request = request)
                call.respond(response)
            }

            delete {
                val currentUserId = call.getCurrentUserId() ?: return@delete call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val poemId = call.getUrlParameter("id").asUUID() ?: return@delete call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid Id"
                )

                val response = poemsRepository.delete(userId = currentUserId, poemId = poemId)
                call.respond(response)
            }

            get("read") {
                val currentUserId = call.getCurrentUserId() ?: return@get call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val poemId = call.getUrlParameter("id").asUUID() ?: return@get call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid Id"
                )

                val response = poemsRepository.markAsRead(userId = currentUserId, poemId = poemId)
                call.respond(response)
            }

            get("bookmark") {
                val currentUserId = call.getCurrentUserId() ?: return@get call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val poemId = call.getUrlParameter("id").asUUID() ?: return@get call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid Id"
                )

                val response = bookmarkRepository.create(userId = currentUserId, poemId = poemId)
                call.respond(response)
            }

            get("unbookmark") {
                val currentUserId = call.getCurrentUserId() ?: return@get call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val poemId = call.getUrlParameter("id").asUUID() ?: return@get call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid Id"
                )

                val response = bookmarkRepository.delete(userId = currentUserId, poemId = poemId)
                call.respond(response)
            }

            get("like") {
                val currentUserId = call.getCurrentUserId() ?: return@get call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val poemId = call.getUrlParameter("id").asUUID() ?: return@get call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid Id"
                )

                val response = likeRepository.likePoem(userId = currentUserId, poemId = poemId)
                call.respond(response)
            }

            get("unlike") {
                val currentUserId = call.getCurrentUserId() ?: return@get call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val poemId = call.getUrlParameter("id").asUUID() ?: return@get call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid Id"
                )

                val response = likeRepository.unlikePoem(userId = currentUserId, poemId = poemId)
                call.respond(response)
            }

            route("comments") {

                post("create") {
                    val currentUserId = call.getCurrentUserId() ?: return@post call.defaultResponse(
                        status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                    )

                    val poemId = call.getUrlParameter("id").asUUID() ?: return@post call.defaultResponse(
                        status = HttpStatusCode.BadRequest, message = "invalid poem id"
                    )

                    val request = call.receive<CommentRequest>()

                    val response =
                        commentRepository.create(userID = currentUserId, poemId = poemId, request = request)
                    call.respond(response)
                }

                get {

                    val currentUserId = call.getCurrentUserId() ?: return@get call.defaultResponse(
                        status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                    )

                    val poemId = call.getUrlParameter("id").asUUID() ?: return@get call.defaultResponse(
                        status = HttpStatusCode.BadRequest, message = "Invalid Id"
                    )

                    val response = commentRepository.getPoemComments(userId = currentUserId, poemId = poemId)
                    call.respond(response)

                }

                route("{comment}") {

                    get {
                        val currentUserId = call.getCurrentUserId() ?: return@get call.defaultResponse(
                            status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                        )

                        val commentId = call.getUrlParameter("id").asUUID() ?: return@get call.defaultResponse(
                            status = HttpStatusCode.BadRequest, message = "invalid comment Id"
                        )

                        val response = commentRepository.get(userId = currentUserId, commentId = commentId)
                        call.respond(response)

                    }

                    put {
                        val currentUserId = call.getCurrentUserId() ?: return@put call.defaultResponse(
                            status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                        )

                        val commentId = call.getUrlParameter("comment").asUUID() ?: return@put call.defaultResponse(
                            status = HttpStatusCode.BadRequest, message = "Invalid Id"
                        )

                        val request = call.receive<CommentRequest>()

                        val response =
                            commentRepository.update(userId = currentUserId, commentId = commentId, request = request)
                        call.respond(response)
                    }

                    delete {

                        val currentUserId = call.getCurrentUserId() ?: return@delete call.defaultResponse(
                            status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                        )

                        val commentId = call.getUrlParameter("comment").asUUID() ?: return@delete call.defaultResponse(
                            status = HttpStatusCode.BadRequest, message = "Invalid Id"
                        )

                        val response = commentRepository.delete(userId = currentUserId, commentId = commentId)
                        call.respond(response)

                    }

                    get("like") {
                        val currentUserId = call.getCurrentUserId() ?: return@get call.defaultResponse(
                            status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                        )

                        val commentId = call.getUrlParameter("comment").asUUID() ?: return@get call.defaultResponse(
                            status = HttpStatusCode.BadRequest, message = "Invalid Id"
                        )

                        val response = likeRepository.likeComment(userId = currentUserId, commentId = commentId)
                        call.respond(response)
                    }

                    get("unlike") {
                        val currentUserId = call.getCurrentUserId() ?: return@get call.defaultResponse(
                            status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                        )

                        val poemId = call.getUrlParameter("id").asUUID() ?: return@get call.defaultResponse(
                            status = HttpStatusCode.BadRequest, message = "Invalid Id"
                        )

                        val response = likeRepository.unlikeComment(userId = currentUserId, poemId = poemId)
                        call.respond(response)
                    }

                }

            }

        }

    }

}