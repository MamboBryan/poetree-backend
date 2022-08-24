package com.mambo.data.helpers

import io.ktor.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object PasswordProvider {

    private val secret = System.getenv("SECRET_KEY")

    fun generatePasswordHash(password: String): String {
        val key = hex(secret)
        val hmacKey = SecretKeySpec(key, "HmacSHA1")
        val hmac = Mac.getInstance("HmacSHA1")
        hmac.init(hmacKey)
        return hex(hmac.doFinal(password.toByteArray(Charsets.UTF_8)))
    }
}