package com.mambobryan.utils

import com.mambobryan.data.models.ServerResponse
import io.ktor.http.*

fun defaultBadRequestResponse(message: String = "Bad Request") = ServerResponse(
    status = HttpStatusCode.BadRequest, message = message, data = Any() ?: null
)

fun defaultOkResponse(message: String = "success", data: Any?) = ServerResponse(
    status = HttpStatusCode.OK, message = message, data = data
)

fun serverErrorResponse(message: String = "error") = ServerResponse(
    status = HttpStatusCode.InternalServerError, message = message, data = null
)
