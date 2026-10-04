package com.example.plugins

import com.example.models.responses.ErrorResponse
import com.example.models.responses.ApiResponse
import io.ktor.server.plugins.BadRequestException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {

    install(StatusPages) {

        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(error = "Invalid request body"))
        }

        exception<Throwable> { call, cause ->

            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    error = cause.message ?: "Unknown error"
                )
            )
        }
    }
}
