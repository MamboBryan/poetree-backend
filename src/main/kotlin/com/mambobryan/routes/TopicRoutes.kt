package com.mambobryan.routes

import com.mambobryan.data.requests.TopicRequest
import com.mambobryan.repositories.TopicsRepository
import com.mambobryan.utils.defaultResponse
import com.mambobryan.utils.getUrlParameter
import com.mambobryan.utils.respond
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.topicRoutes() {

    val repository = TopicsRepository()

    route("topics") {

        post("create") {

            val request = call.receive<TopicRequest>()

            val response = repository.create(request)
            call.respond(response)

        }

        get {

            val response = repository.getTopics()
            call.respond(response)

        }

        route("{id}") {

            get {

                val id = call.getUrlParameter("id") ?: return@get call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid Id"
                )

                val topic = repository.get(topicId = id.toInt())
                call.respond(topic)

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