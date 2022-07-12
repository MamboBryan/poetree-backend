package com.mambobryan.data.requests

data class UserUpdateRequest(
    val email: String?,
    val username: String?,
    val bio: String?,
    val dateOfBirth: String?,
    val gender: Int?
)