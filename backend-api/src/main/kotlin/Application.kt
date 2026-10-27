package com.example

import io.ktor.server.engine.*
import io.ktor.server.application.*
import com.example.database.DatabaseFactory
import io.ktor.serialization.kotlinx.json.*
import com.example.routes.authRoutes
import com.example.routes.itemRoutes
import com.example.routes.uploadRoutes
import com.example.routes.reorderRoutes
import io.ktor.server.routing.*
import com.example.plugins.configureSerialization
import com.example.plugins.configureAuthentication
import com.example.plugins.configureStatusPages
import com.example.repositories.ReorderRepository
import com.example.services.ReorderProcessor
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.http.content.*
import java.io.File


fun main() {
    embeddedServer(
        io.ktor.server.netty.Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init(environment.config)

    install(CallLogging)

    configureSerialization()
    configureStatusPages()
    configureAuthentication()

    val reorderProcessor = ReorderProcessor(ReorderRepository())
    reorderProcessor.start(this)

    routing {
        itemRoutes()
        uploadRoutes()
        authRoutes()
        reorderRoutes(reorderProcessor)
        staticFiles("/uploads", File("uploads"))
    }
}
