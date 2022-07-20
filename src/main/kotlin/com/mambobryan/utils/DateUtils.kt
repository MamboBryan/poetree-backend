package com.mambobryan.utils

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
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

fun Date?.asLocalDate(): LocalDate? {
    if (this == null) return null
    return Instant.ofEpochMilli(this.time).atZone(ZoneId.systemDefault()).toLocalDate()
}

fun Date?.asLocalDateTime(): LocalDateTime? {
    if (this == null) return null
    return Instant.ofEpochMilli(this.time).atZone(ZoneId.systemDefault()).toLocalDateTime()
}
fun Date.isValidAge(): Boolean {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance()
    then.time = this
    val years = now[Calendar.YEAR] - then[Calendar.YEAR]
    return years >= 15
}

fun LocalDate?.toDate(): Date? {
    if (this == null) return null
    return Date.from(this.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())
}

fun LocalDateTime?.toDate(): Date? {
    if (this == null) return null
    return Date.from(this.atZone(ZoneId.systemDefault()).toInstant())
}
