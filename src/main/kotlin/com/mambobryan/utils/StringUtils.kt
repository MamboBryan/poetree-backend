package com.mambobryan.utils

import java.util.*

fun String?.isValidEmail(): Boolean {
    if (isNullOrBlank()) return false
    val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})";
    return emailRegex.toRegex().matches(this);
}

fun String?.isValidPassword(): Boolean {
    if (isNullOrBlank()) return false
    val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=])(?=\\\\S+\$).{4,}\$"
    return passwordRegex.toRegex().matches(this);
}

fun String?.asUUID(): UUID? {
    if (this.isNullOrBlank()) return null
    return try {
        UUID.fromString(this)
    } catch (e: IllegalArgumentException) {
        println(e.localizedMessage)
        null
    }
}

fun UUID?.asString(): String? = this?.toString()