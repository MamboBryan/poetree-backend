package com.mambobryan.utils

import com.mambobryan.data.models.ServerResponse
import io.ktor.http.*

fun defaultOkResponse(message: String = "success", data: Any?) = ServerResponse(
    status = HttpStatusCode.OK, message = message, data = data
)

fun defaultCreatedResponse(message: String = "success", data: Any?) = ServerResponse(
    status = HttpStatusCode.Created, message = message, data = data
)

fun defaultBadRequestResponse(message: String = "Bad Request") = ServerResponse(
    status = HttpStatusCode.BadRequest, message = message, data = Any() ?: null
)

fun defaultNotFoundResponse(message: String = "Bad Request") = ServerResponse(
    status = HttpStatusCode.NotFound, message = message, data = Any() ?: null
)

fun serverErrorResponse(message: String = "error", data: Any? = null) = ServerResponse(
    status = HttpStatusCode.InternalServerError, message = message, data = data
)
