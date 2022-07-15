package com.mambobryan.routes

import com.mambobryan.data.requests.AuthRequest
import com.mambobryan.data.requests.ResetRequest
import com.mambobryan.data.tables.user.User
import com.mambobryan.plugins.generateToken
import com.mambobryan.plugins.hash
import com.mambobryan.repositories.UsersRepository
import com.mambobryan.utils.defaultResponse
import com.mambobryan.utils.respond
import com.mambobryan.utils.successWithData
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

                val user = when (response.status.isSuccess()) {
                    true -> response.data as User
                    else -> return@post call.respond(response)
                }

                val hash = hashFunction(request.password!!)

                when (user.hash == hash) {
                    true -> {
                        val token = generateToken(issuer = issuer, audience = audience, user = user)

                        val data = mapOf(
                            "token" to token, "user" to user
                        )

                        call.successWithData(
                            status = HttpStatusCode.OK, message = "Signed Up successfully", data = data
                        )
                    }
                    false -> call.defaultResponse(
                        status = HttpStatusCode.NotAcceptable, message = "Invalid Credentials"
                    )
                }

            } catch (e: Exception) {
                call.defaultResponse(
                    status = HttpStatusCode.NotAcceptable, message = "Failed Signing in"
                )
            }

        }

        post("signup") {

            val request = call.receive<AuthRequest>()

            if (request.email.isNullOrBlank() or request.password.isNullOrBlank()) return@post call.defaultResponse(
                status = HttpStatusCode.BadRequest, message = "Email or Password cannot be blank"
            )

            try {

                val hash = hashFunction(request.password!!)

                val response = repository.create(email = request.email!!, hash = hash)

                val user = when (response.status.isSuccess()) {
                    true -> response.data as User
                    else -> return@post call.respond(response)
                }

                val token = generateToken(issuer = issuer, audience = audience, user = user)

                val data = mapOf(
                    "token" to token, "user" to user
                )

                call.successWithData(
                    status = HttpStatusCode.Created, message = "Signed Up successfully", data = data
                )

            } catch (e: Exception) {
                return@post call.defaultResponse(
                    status = HttpStatusCode.NotAcceptable, message = "Failed signing up"
                )
            }

        }

        post("reset") {

            val request = call.receive<ResetRequest>()

            if (request.email.isNullOrBlank()) return@post call.defaultResponse(
                status = HttpStatusCode.BadRequest, message = "Email cannot be blank"
            )

            return@post call.defaultResponse(status = HttpStatusCode.OK, message = "Rest Link Sent")

        }

    }

}