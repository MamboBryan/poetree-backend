package com.mambo

import com.mambo.data.DatabaseFactory
import io.ktor.server.application.*
import com.mambo.application.plugins.*
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {

    Napier.base(DebugAntilog())
    DatabaseFactory.init()

    configureSecurity()
    configureRouting()
    configureSerialization()

}
