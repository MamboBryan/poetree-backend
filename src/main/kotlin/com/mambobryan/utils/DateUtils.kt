package com.mambobryan.utils

import java.text.SimpleDateFormat
import java.util.*

val dateFormat = SimpleDateFormat("dd-MM-yyyy")
val dateTimeFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")

fun Long?.toDate(): String? {
    if (this == null) return null
    return dateTimeFormat.format(Date(this))
}

fun Long?.toDateAndTime(): String? {
    if (this == null) return null
    return dateTimeFormat.format(Date(this))
}

fun Date?.toDateString(): String? {
    if (this == null) return null
    return dateFormat.format(this)
}

fun Date?.toDateTimeString(): String? {
    if (this == null) return null
    return dateFormat.format(this)
}