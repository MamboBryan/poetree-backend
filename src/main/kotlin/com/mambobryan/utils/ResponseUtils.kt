package com.mambobryan.utils

import com.mambobryan.data.Response
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

suspend fun ApplicationCall.defaultResponse(status: HttpStatusCode, message: String = "Error") {
    return this.respond(
        status = status, message = Response(success = status.isSuccess(), message = message, data = null)
    )
}

suspend fun <T> ApplicationCall.successWithData(status: HttpStatusCode, message: String = "Error", data: T) {
    return this.respond(
        status = status, message = Response(success = status.isSuccess(), message = message, data = data)
    )
}

fun ApplicationCall.getUserId(): Int? {
    val principal = this.principal<JWTPrincipal>()
    return principal?.payload?.getClaim("id")?.asInt()
}