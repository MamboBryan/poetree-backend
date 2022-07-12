package com.mambobryan.plugins

import com.mambobryan.routes.authRoutes
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()

    routing {

        route("v1") {

            authRoutes(issuer = issuer, audience = audience)

            authenticate("auth-jwt") {

            }

        }

    }

}
