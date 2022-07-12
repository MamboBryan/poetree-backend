package com.mambobryan.data.requests

data class PasswordResetRequest(
    val oldPassword: String?,
    val newPassword: String?
)