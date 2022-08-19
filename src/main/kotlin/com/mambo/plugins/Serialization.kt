package com.mambo.plugins

import com.mambo.utils.AnnotationExclusionStrategy
import io.ktor.serialization.gson.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.application.*

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        gson {
            this.setExclusionStrategies(AnnotationExclusionStrategy()).serializeNulls().create()
        }
    }
}
