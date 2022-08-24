package com.mambobryan.routes

import com.mambobryan.data.requests.CommentRequest
import com.mambobryan.data.requests.PoemRequest
import com.mambobryan.repositories.BookmarkRepository
import com.mambobryan.repositories.CommentsRepository
import com.mambobryan.repositories.LikeRepository
import com.mambobryan.repositories.PoemsRepository
import com.mambobryan.utils.*
import com.mambobryan.utils.respond
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.poemRoutes() {

    val poemsRepository = PoemsRepository()
    val bookmarkRepository = BookmarkRepository()
    val likeRepository = LikeRepository()
    val commentRepository = CommentsRepository()

    route("poems") {

        // create poem
        post {

            val currentUserId = call.getCurrentUserId() ?: return@post call.defaultResponse(
                status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
            )

            val request = call.receive<PoemRequest>()

            val response = poemsRepository.create(userId = currentUserId, request = request)

            call.respond(response)

        }

        // get poem list, enables searching by string (q), topic and page
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

        route("poem") {

            // get poem
            post {

                val currentUserId = call.getCurrentUserId() ?: return@post call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val request = call.receive<PoemRequest>()

                val poemId = request.poemId.asUUID() ?: return@post call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid poem id"
                )

                val response = poemsRepository.getPoem(userId = currentUserId, poemId = poemId)
                call.respond(response)
            }

            // update poem
            put {
                val currentUserId = call.getCurrentUserId() ?: return@put call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val request = call.receive<PoemRequest>()

                val response = poemsRepository.update(userId = currentUserId, request = request)
                call.respond(response)

            }

            // delete poem
            delete {
                val currentUserId = call.getCurrentUserId() ?: return@delete call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val request = call.receive<PoemRequest>()

                val poemId = request.poemId.asUUID() ?: return@delete call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid poem id"
                )

                val response = poemsRepository.delete(userId = currentUserId, poemId = poemId)
                call.respond(response)
            }

            // mark poem as read
            post("read") {
                val currentUserId = call.getCurrentUserId() ?: return@post call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val request = call.receive<PoemRequest>()

                val poemId = request.poemId.asUUID() ?: return@post call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid poem id"
                )

                val response = poemsRepository.markAsRead(userId = currentUserId, poemId = poemId)
                call.respond(response)
            }

            // bookmark poem
            post("bookmark") {
                val currentUserId = call.getCurrentUserId() ?: return@post call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val request = call.receive<PoemRequest>()

                val poemId = request.poemId.asUUID() ?: return@post call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid poem id"
                )

                val response = bookmarkRepository.create(userId = currentUserId, poemId = poemId)
                call.respond(response)
            }

            // un-bookmark poem
            delete("un-bookmark") {
                val currentUserId = call.getCurrentUserId() ?: return@delete call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val request = call.receive<PoemRequest>()

                val poemId = request.poemId.asUUID() ?: return@delete call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid poem id"
                )

                val response = bookmarkRepository.delete(userId = currentUserId, poemId = poemId)
                call.respond(response)
            }

            // like poem
            post("like") {
                val currentUserId = call.getCurrentUserId() ?: return@post call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val request = call.receive<PoemRequest>()

                val poemId = request.poemId.asUUID() ?: return@post call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid poem id"
                )

                val response = likeRepository.likePoem(userId = currentUserId, poemId = poemId)
                call.respond(response)
            }

            // unlike a poem
            delete("unlike") {
                val currentUserId = call.getCurrentUserId() ?: return@delete call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val request = call.receive<PoemRequest>()

                val poemId = request.poemId.asUUID() ?: return@delete call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid poem id"
                )

                val response = likeRepository.unlikePoem(userId = currentUserId, poemId = poemId)
                call.respond(response)
            }

            // poem comments
            post("comments") {
                val currentUserId = call.getCurrentUserId() ?: return@post call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val page = call.getQuery("page")?.toIntOrNull() ?: 1

                val request = call.receive<CommentRequest>()

                val poemId = request.poemId.asUUID() ?: return@post call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid poem id"
                )

                val response = commentRepository.getComments(userId = currentUserId, poemId = poemId, page = page)
                call.respond(response)
            }

        }


    }

}