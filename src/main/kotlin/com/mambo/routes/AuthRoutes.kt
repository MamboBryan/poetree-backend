package com.mambo.routes

import com.mambo.data.requests.AuthRequest
import com.mambo.data.requests.ResetRequest
import com.mambo.data.tables.user.User
import com.mambo.data.tables.user.toUserDto
import com.mambo.plugins.generateToken
import com.mambo.plugins.hash
import com.mambo.repositories.UsersRepository
import com.mambo.utils.defaultResponse
import com.mambo.utils.isValidPassword
import com.mambo.utils.respond
import com.mambo.utils.successWithData
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.authRoutes(
    issuer: String, audience: String
) {

    val repository = UsersRepository()
    val hashFunction: (String) -> String = { pass: String -> hash(password = pass) }

    route("auth") {

        post("signin") {

            val request = call.receive<AuthRequest>()

            if (request.email.isNullOrBlank() or request.password.isNullOrBlank()) return@post call.defaultResponse(
                status = HttpStatusCode.BadRequest, message = "Email or Password cannot be blank"
            )

            return@post try {

                val response = repository.getUserByEmail(request.email!!)

                if (response.status.isSuccess().not()) return@post call.respond(response)

                val user = response.data as User

                val hash = hashFunction(request.password!!)

                when (user.hash == hash) {
                    true -> {
                        val token = generateToken(issuer = issuer, audience = audience, user = user)

                        val data = mapOf(
                            "token" to token, "user" to user.toUserDto()
                        )

                        call.successWithData(
                            status = HttpStatusCode.OK, message = "Signed Up successfully", data = data
                        )
                    }

                    false -> call.defaultResponse(
                        status = HttpStatusCode.Unauthorized, message = "Invalid Credentials"
                    )
                }

            } catch (e: Exception) {
                call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Failed Signing in"
                )
            }

        }

        post("signup") {

            val request = call.receive<AuthRequest>()

            if (request.email.isNullOrBlank() or request.password.isNullOrBlank()) return@post call.defaultResponse(
                status = HttpStatusCode.BadRequest, message = "Email or Password cannot be blank"
            )

            if (request.password.isValidPassword().not()) return@post call.defaultResponse(
                status = HttpStatusCode.BadRequest,
                message = "Password must be a minimum of 8 characters containing Uppercase, Lowercase, Number and Special Character"
            )

            return@post try {

                val hash = hashFunction(request.password!!)

                val response = repository.create(email = request.email!!, hash = hash)

                if (response.status.isSuccess().not()) return@post call.respond(response)

                val user = response.data as User

                val token = generateToken(issuer = issuer, audience = audience, user = user)

                val data = mapOf(
                    "token" to token, "user" to user.toUserDto()
                )

                call.successWithData(
                    status = HttpStatusCode.Created, message = "Signed Up successfully", data = data
                )

            } catch (e: Exception) {

                call.defaultResponse(
                    status = HttpStatusCode.Unauthorized, message = "Failed signing up"
                )
            }

        }

        post("reset") {

            val request = call.receive<ResetRequest>()

            if (request.email.isNullOrBlank()) return@post call.defaultResponse(
                status = HttpStatusCode.BadRequest, message = "Email cannot be blank"
            )

            /**
             * TODO
             *  1. verify user with email exists
             *  1.a if user with email doesn't exist return error
             *  1.b if user with email exists return true
             *  2. generate random 8 strong digit password
             *  3. send password to user email
             *  4. respond with either success or failure
             */

            return@post call.defaultResponse(status = HttpStatusCode.OK, message = "Rest Link Sent")

        }

    }

}