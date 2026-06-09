package com.example.validation

import com.example.models.responses.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

suspend fun ApplicationCall.respondValidationError(message: String) {
    respond(
        HttpStatusCode.BadRequest,
        ApiResponse<Unit>(
            data = null,
            error = message
        )
    )
}
suspend fun ApplicationCall.respondNotFoundError(message: String) {
    respond(
        HttpStatusCode.NotFound,
        ApiResponse<Unit>(
            data = null,
            error = message
        )
    )
}