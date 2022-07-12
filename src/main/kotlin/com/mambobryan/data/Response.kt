package com.mambobryan.data

data class Response<T>(
    val success: Boolean, val message: String, val data: T
)