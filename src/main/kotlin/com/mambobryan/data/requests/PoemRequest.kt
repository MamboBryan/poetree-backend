package com.mambobryan.data.requests

data class PoemRequest(
    val title: String?,
    val content: String?,
    val html: String?,
    val topic: Int?,
)