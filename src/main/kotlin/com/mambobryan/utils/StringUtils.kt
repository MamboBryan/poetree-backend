package com.mambobryan.utils

import java.util.*

fun String?.isValidEmail(): Boolean {
    if (isNullOrBlank()) return false
    val EMAIL_REGEX = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})";
    return EMAIL_REGEX.toRegex().matches(this);
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