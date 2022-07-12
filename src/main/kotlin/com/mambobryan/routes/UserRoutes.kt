package com.mambobryan.routes

import com.mambobryan.data.requests.UserUpdateRequest
import com.mambobryan.repositories.UsersRepository
import com.mambobryan.utils.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.userRoutes() {

    val repository = UsersRepository()

    route("users") {

        route("me") {

            get {
                val userId = call.getUserId() ?: return@get call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val user = repository.getUser(userId = userId) ?: return@get call.defaultResponse(
                    status = HttpStatusCode.NotFound, message = "I don't know you!",
                )

                call.successWithData(status = HttpStatusCode.OK, message = "success", data = user)

            }

            post("setup") {

                val userId = call.getUserId() ?: return@post call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val request = call.receive<UserUpdateRequest>()

                if (request.username.isNullOrBlank() or request.bio.isNullOrBlank() or request.dateOfBirth.isNullOrBlank() or (request.gender == null)) return@post call.defaultResponse(
                    status = HttpStatusCode.BadRequest, "Enter all fields to continue"
                )

                val date = request.dateOfBirth.toDate() ?: return@post call.defaultResponse(
                    status = HttpStatusCode.BadRequest, "Invalid date parsed"
                )

                if (date.isValidAge().not()) return@post call.defaultResponse(
                    status = HttpStatusCode.BadRequest, "User Should be 15 years or older"
                )

                if ((request.gender == null) or (request.gender !in 0..1)) return@post call.defaultResponse(
                    status = HttpStatusCode.BadRequest, "Invalid gender"
                )

                val user = repository.update(
                    id = userId,
                    email = request.email,
                    username = request.username,
                    bio = request.bio,
                    dateOfBirth = request.dateOfBirth,
                    gender = request.gender
                ) ?: return@post call.defaultResponse(
                    status = HttpStatusCode.NotFound, message = "User Not Found",
                )

                call.successWithData(
                    status = HttpStatusCode.Created, message = "User setup successfully", data = user
                )

            }

            put("update") {

                val userId = call.getUserId() ?: return@put call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val request = call.receive<UserUpdateRequest>()

                if (
                    request.username.isNullOrBlank() &&
                    request.bio.isNullOrBlank() &&
                    request.email.isNullOrBlank() &&
                    request.dateOfBirth.isNullOrBlank() &&
                    (request.gender == null)
                ) return@put call.defaultResponse(
                    status = HttpStatusCode.BadRequest, "Enter all field to continue"
                )

                if (!request.email.isValidEmail()) return@put call.defaultResponse(
                    status = HttpStatusCode.BadRequest, "Invalid Email"
                )

                if ((request.gender != null) && (request.gender !in 0..1)) return@put call.defaultResponse(
                    status = HttpStatusCode.BadRequest, "Invalid gender"
                )

                if (request.dateOfBirth != null) {

                    val date = request.dateOfBirth.toDate() ?: return@put call.defaultResponse(
                        status = HttpStatusCode.BadRequest, "Invalid date parsed"
                    )

                    if (date.isValidAge().not()) return@put call.defaultResponse(
                        status = HttpStatusCode.BadRequest, "User Should be 15 years or older"
                    )
                }

                val user = repository.update(
                    id = userId,
                    email = request.email,
                    username = request.username,
                    bio = request.bio,
                    dateOfBirth = request.dateOfBirth,
                    gender = request.gender
                ) ?: return@put call.defaultResponse(
                    status = HttpStatusCode.NotFound, message = "User Not Found",
                )

                call.successWithData(
                    status = HttpStatusCode.Created, message = "User updated successfully", data = user
                )
            }

        }

    }

}