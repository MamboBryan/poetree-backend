package com.mambobryan.utils

import com.mambobryan.data.models.ServerResponse
import io.ktor.http.*

suspend fun <T : Any?> safeTransaction(
    error: String = "error",
    block: suspend () -> ServerResponse<T?>
): ServerResponse<T?> {
    return try {
        block()
    } catch (e: Exception) {
        println(e.localizedMessage)
        ServerResponse(status = HttpStatusCode.InternalServerError, message = error, data = null)
    }
}