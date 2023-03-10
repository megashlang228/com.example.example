package com.example

import com.example.database.DatabaseFactory
import com.example.network.routing.configureAuthRouting
import com.example.network.routing.configurePostRouting
import com.example.network.routing.configureRouting
import io.ktor.server.application.*
import com.example.plugins.*
import com.example.secure.configureSecurity

fun main(args: Array<String>): Unit =
    io.ktor.server.cio.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {

    DatabaseFactory.init()

    configureWebsocket()
    configureSerialization()
    configureMonitoring()
    configureSecurity()
    configureRouting()
}
