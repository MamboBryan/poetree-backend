package com.mambobryan.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mambobryan.data.tables.user.User
import com.mambobryan.data.tables.user.UserDto
import com.mambobryan.utils.asString
import com.mambobryan.utils.defaultResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.util.*
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private val secret = System.getenv("SECRET_KEY")

private val algorithm = Algorithm.HMAC512(secret)

private fun expiresAt(): Date {
    val now = Calendar.getInstance()
    now.set(Calendar.DAY_OF_YEAR, now[Calendar.DAY_OF_YEAR] + 30)
    return now.time
}

fun hash(password: String): String {
    val key = hex(secret)
    val hmacKey = SecretKeySpec(key, "HmacSHA1")
    val hmac = Mac.getInstance("HmacSHA1")
    hmac.init(hmacKey)
    return hex(hmac.doFinal(password.toByteArray(Charsets.UTF_8)))
}

fun getVerifier(audience: String, issuer: String) = JWT
    .require(algorithm)
    .withAudience(audience)
    .withIssuer(issuer)
    .build()

fun generateToken(issuer: String, audience: String, user: User): String = JWT
    .create()
    .withAudience(audience)
    .withIssuer(issuer)
    .withExpiresAt(expiresAt())
    .withClaim("id", user.id.asString())
    .sign(algorithm)

fun setTokenExpiry(issuer: String, audience: String,keyId: String) = JWT
    .create()
    .withAudience(audience)
    .withIssuer(issuer)
    .withKeyId(keyId)
    .withExpiresAt(Date())
    .sign(algorithm)

fun Application.configureSecurity() {

    val jwtAudience = environment.config.property("jwt.audience").getString()
    val jwtIssuer = environment.config.property("jwt.issuer").getString()
    val jwtRealm = environment.config.property("jwt.realm").getString()

    install(Authentication) {
        jwt("auth-jwt") {

            realm = jwtRealm

            verifier(getVerifier(audience = jwtAudience, issuer = jwtIssuer))

            validate { credential ->

                val it = credential.payload.getClaim("id").asString()
                when (it != null) {
                    true -> JWTPrincipal(credential.payload)
                    else -> null
                }
            }

            challenge { defaultScheme, realm ->
                return@challenge call.defaultResponse(status = HttpStatusCode.Unauthorized, message = "Unauthorized")
            }

        }
    }

}
