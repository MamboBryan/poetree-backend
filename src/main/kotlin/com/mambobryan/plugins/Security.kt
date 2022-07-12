package com.mambobryan.plugins

import io.ktor.server.auth.*
import io.ktor.util.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.mambobryan.data.models.User
import com.mambobryan.utils.defaultResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private val secret = System.getenv("SECRET_KEY")

private val algorithm = Algorithm.HMAC512(secret)

private fun expiresAt() = Date(System.currentTimeMillis() + 3_600_000 * 24)

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
    .withClaim("id", user.id)
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

                val it = credential.payload.getClaim("id").asInt()

                when (it != null) {
                    true -> JWTPrincipal(credential.payload)
                    else -> null
                }
            }
            challenge { defaultScheme, realm ->
                call.defaultResponse(status = HttpStatusCode.Unauthorized, message = "Unauthorized")
            }
        }
    }

}
