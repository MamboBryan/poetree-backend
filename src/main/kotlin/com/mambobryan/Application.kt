package com.mambobryan

import com.mambobryan.data.Database
import io.ktor.server.application.*
import com.mambobryan.plugins.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {

    Database.init()

    configureRouting()
    configureSerialization()
    configureSecurity()

}
