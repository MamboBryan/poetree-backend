package com.mambobryan.utils

fun String?.isValidEmail(): Boolean {
    if (isNullOrBlank()) return false
    val EMAIL_REGEX = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})";
    return EMAIL_REGEX.toRegex().matches(this);
}