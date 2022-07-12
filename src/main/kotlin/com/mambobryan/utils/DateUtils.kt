package com.mambobryan.utils

import java.text.SimpleDateFormat
import java.util.*

val dateFormat = SimpleDateFormat("dd-MM-yyyy")
val dateTimeFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")

fun Long?.toDateLong(): String? {
    if (this == null) return null
    return dateFormat.format(Date(this))
}

fun Long?.toDateAndTime(): String? {
    if (this == null) return null
    return dateTimeFormat.format(Date(this))
}

fun String?.toDateLong(): Long? {
    if (this == null) return null
    return dateFormat.parse(this).time
}

fun String?.toDate(): Date? {
    if (this.isNullOrBlank()) return null
    return dateFormat.parse(this)
}

fun String?.toDateTime(): Long? {
    if (this == null) return null
    return dateTimeFormat.parse(this).time
}

fun Date?.toDateString(): String? {
    if (this == null) return null
    return dateFormat.format(this)
}

fun Date?.toDateTimeString(): String? {
    if (this == null) return null
    return dateFormat.format(this)
}

fun Date.isValidAge(): Boolean {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance()
    then.time = this
    val years = now[Calendar.YEAR] - then[Calendar.YEAR]
    return years >= 15
}