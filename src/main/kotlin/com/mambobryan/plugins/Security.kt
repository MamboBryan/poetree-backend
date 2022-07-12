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

fun generateToken(issuer: String, audience: String, user: User): String = JWT
    .create()
    .withAudience(audience)
    .withIssuer(issuer)
    .withExpiresAt(expiresAt())
    .withClaim("id", user.id)
    .sign(algorithm)

fun Application.configureSecurity() {

    authentication {
        jwt {
            val jwtAudience = this@configureSecurity.environment.config.property("jwt.audience").getString()
            realm = this@configureSecurity.environment.config.property("jwt.realm").getString()
            verifier(
                JWT.require(algorithm)
                    .withAudience(jwtAudience)
                    .withIssuer(this@configureSecurity.environment.config.property("jwt.domain").getString())
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
            }
            challenge { defaultScheme, realm ->
                call.defaultResponse(status = HttpStatusCode.Unauthorized, message = "Unauthorized")
            }
        }
    }

}
