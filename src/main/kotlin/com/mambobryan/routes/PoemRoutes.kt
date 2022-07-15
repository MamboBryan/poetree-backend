package com.mambobryan.routes

import com.mambobryan.data.requests.PoemRequest
import com.mambobryan.repositories.BookmarkRepository
import com.mambobryan.repositories.PoemsRepository
import com.mambobryan.utils.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.poemRoutes() {

    val poemsRepository = PoemsRepository()
    val bookmarkRepository = BookmarkRepository()

    route("poems") {

        post("create") {

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

            val response = poemsRepository.getPoems(userId = currentUserId)
            call.respond(response)

        }

        get("search") {

            val currentUserId = call.getCurrentUserId() ?: return@get call.defaultResponse(
                status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
            )

            val query = call.getQuery("query")

            val topic = call.getQuery("topic")

            val response = when {
                query != null && topic != null -> poemsRepository.getPoems(
                    userId = currentUserId, topic = topic.toInt(), query = query
                )
                query != null -> poemsRepository.getPoems(userId = currentUserId, query = query)
                topic != null -> poemsRepository.getPoems(userId = currentUserId, topic = topic.toInt())
                else -> poemsRepository.getPoems(userId = currentUserId)
            }

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

        }

    }

}