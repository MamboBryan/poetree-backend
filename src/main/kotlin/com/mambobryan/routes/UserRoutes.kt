package com.mambobryan.routes

import com.mambobryan.data.requests.PasswordResetRequest
import com.mambobryan.data.requests.UserUpdateRequest
import com.mambobryan.plugins.generateToken
import com.mambobryan.plugins.hash
import com.mambobryan.repositories.UsersRepository
import com.mambobryan.utils.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.userRoutes(
    issuer: String, audience: String
) {

    val repository = UsersRepository()

    route("users") {

        get {

            val userId = call.getCurrentUserId() ?: return@get call.defaultResponse(
                status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
            )

            val users = repository.getUsers(id = userId)
            when (users.isEmpty()) {
                true -> call.defaultResponse( status = HttpStatusCode.OK, message = "No User found")
                false -> call.successWithData(status = HttpStatusCode.OK, message = "Users Found", data = users)
            }

        }

        get("search") {

            val userId = call.getCurrentUserId() ?: return@get call.defaultResponse(
                status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
            )

            val query = call.getQuery(queryName = "query") ?: return@get call.defaultResponse(
                status = HttpStatusCode.BadRequest, message = "Bad Request",
            )

            val users = repository.getUsers(userId = userId, query = query)
            when (users.isEmpty()) {
                true -> call.defaultResponse(message = "No User found", status = HttpStatusCode.OK)
                false -> call.successWithData(
                    status = HttpStatusCode.OK, message = "Users Found", data = users
                )
            }

        }

        route("me") {

            get {
                val userId = call.getCurrentUserId() ?: return@get call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val user = repository.getUser(userId = userId) ?: return@get call.defaultResponse(
                    status = HttpStatusCode.NotFound, message = "I don't know you!",
                )

                call.successWithData(status = HttpStatusCode.OK, message = "success", data = user)

            }

            post("setup") {

                val userId = call.getCurrentUserId() ?: return@post call.defaultResponse(
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

                val userId = call.getCurrentUserId() ?: return@put call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val request = call.receive<UserUpdateRequest>()

                if (
                    request.username.isNullOrBlank() &&
                    request.bio.isNullOrBlank() &&
                    request.email.isNullOrBlank() &&
                    request.dateOfBirth.isNullOrBlank() &&
                    request.imageUrl.isNullOrBlank() &&
                    request.token.isNullOrBlank() &&
                    (request.gender == null)
                ) return@put call.defaultResponse(
                    status = HttpStatusCode.BadRequest, "Enter all field to continue"
                )

                if (request.email != null && !request.email.isValidEmail()) return@put call.defaultResponse(
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
                    gender = request.gender,
                    imageUrl = request.imageUrl,
                    token = request.token
                ) ?: return@put call.defaultResponse(
                    status = HttpStatusCode.NotFound, message = "User Not Found",
                )

                call.successWithData(
                    status = HttpStatusCode.Created, message = "User updated successfully", data = user
                )
            }

            put("update-password") {

                val hashFunction: (String) -> String = { pass: String -> hash(password = pass) }

                val userId = call.getCurrentUserId() ?: return@put call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
                )

                val request = call.receive<PasswordResetRequest>()

                if (request.oldPassword.isNullOrBlank() or request.newPassword.isNullOrBlank()) return@put call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Invalid Request Details"
                )

                if (request.oldPassword.equals(request.newPassword)) return@put call.defaultResponse(
                    status = HttpStatusCode.Conflict, message = "New Password cannot be same as old password"
                )

                val user = repository.getUser(userId = userId) ?: return@put call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Unauthorized"
                )

                return@put try {

                    val hash = hashFunction(request.oldPassword!!)

                    when (user.hash == hash) {
                        true -> {
                            val token = generateToken(issuer = issuer, audience = audience, user = user)

                            val data = mapOf(
                                "token" to token, "user" to user
                            )

                            call.successWithData(
                                status = HttpStatusCode.OK, message = "Password updated successfully", data = data
                            )
                        }
                        false -> call.defaultResponse(
                            status = HttpStatusCode.NotAcceptable, message = "Invalid Credentials"
                        )
                    }

                } catch (e: Exception) {
                    call.defaultResponse(
                        status = HttpStatusCode.NotAcceptable, message = "Failed updating password"
                    )
                }

            }

            delete("delete") {
                val id = call.getCurrentUserId() ?: return@delete call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Missing Id",
                )

                return@delete when (repository.delete(id)) {
                    true -> call.defaultResponse(
                        status = HttpStatusCode.BadRequest, message = "Failed deleting user",
                    )

                    false -> call.defaultResponse(
                        status = HttpStatusCode.OK, message = "User deleted",
                    )
                }
            }

        }

        route("{id}") {

            get {

//                val currentUserId = call.getCurrentUserId() ?: return@get call.defaultResponse(
//                    status = HttpStatusCode.Unauthorized, message = "Authentication Failed"
//                )

                val userId = call.getUrlParameter("id") ?: return@get call.defaultResponse(
                    status = HttpStatusCode.BadRequest, message = "Missing Id",
                )

                val user = repository.getUser(userId = userId.toInt()) ?: return@get call.defaultResponse(
                    status = HttpStatusCode.NotFound, message = "User Not Found",
                )

                call.successWithData(status = HttpStatusCode.OK, message = "success", data = user)

            }

        }

    }

}