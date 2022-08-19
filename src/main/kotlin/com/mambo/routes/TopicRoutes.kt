package com.mambo.routes

import com.mambo.data.requests.TopicRequest
import com.mambo.repositories.TopicsRepository
import com.mambo.utils.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.topicRoutes() {

    val repository = TopicsRepository()

    route("topics") {

        post {

            val request = call.receive<TopicRequest>()

            val response = repository.create(request)
            call.respond(response)

        }

        get {

            val page = call.getQuery(QueryUtils.PAGE)?.toInt() ?: 1

            val response = repository.getTopics(page = page)
            call.respond(response)

        }

        route("{id}") {

            get {

                val id = call.getUrlParameter("id") ?: return@get call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid Id"
                )

                val response = repository.get(topicId = id.toInt())
                call.respond(response)

            }


            put {

                val id = call.getUrlParameter("id") ?: return@put call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid Id"
                )

                val request = call.receive<TopicRequest>()

                val response = repository.update(topicId = id.toInt(), request = request)
                call.respond(response)

            }

            delete {

                val id = call.getUrlParameter("id") ?: return@delete call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid Id"
                )

                val response = repository.delete(topicId = id.toInt())
                call.respond(response)

            }

        }

    }

}