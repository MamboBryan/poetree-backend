package com.mambobryan.routes

import com.mambobryan.data.requests.PoemRequest
import com.mambobryan.repositories.PoemsRepository
import com.mambobryan.utils.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.poemRoutes() {

    val repository = PoemsRepository()

    route("poems") {

        post("create") {

            val currentUserId = call.getCurrentUserId() ?: return@post call.defaultResponse(
                status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
            )

            val request = call.receive<PoemRequest>()

            val response = repository.create(userId = currentUserId, request = request)
            call.respond(response)

        }

        get {

            val currentUserId = call.getCurrentUserId() ?: return@get call.defaultResponse(
                status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
            )

            val response = repository.getPoems(userId = currentUserId)
            call.respond(response)

        }

        get("search") {

            val currentUserId = call.getCurrentUserId() ?: return@get call.defaultResponse(
                status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
            )

            val query = call.getQuery("query")

            val topic = call.getQuery("topic")

            val response = when {
                query != null && topic != null -> repository.getPoems(
                    userId = currentUserId, topic = topic.toInt(), query = query
                )
                query != null -> repository.getPoems(userId = currentUserId, query = query)
                topic != null -> repository.getPoems(userId = currentUserId, topic = topic.toInt())
                else -> repository.getPoems(userId = currentUserId)
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

                val response = repository.getPoem(userId = currentUserId, poemId = poemId)
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

                val response = repository.update(userId = currentUserId, poemId = poemId, request = request)
                call.respond(response)
            }

            delete {
                val currentUserId = call.getCurrentUserId() ?: return@delete call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val poemId = call.getUrlParameter("id").asUUID() ?: return@delete call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid Id"
                )

                val response = repository.delete(userId = currentUserId, poemId = poemId)
                call.respond(response)
            }

        }

    }

}