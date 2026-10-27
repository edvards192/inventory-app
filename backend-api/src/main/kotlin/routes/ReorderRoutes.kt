package com.example.routes

import com.example.models.requests.CreateReorderRequest
import com.example.models.responses.ApiResponse
import com.example.repositories.ReorderRepository
import com.example.services.ReorderProcessor
import com.example.validation.respondNotFoundError
import com.example.validation.respondValidationError
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.reorderRoutes(reorderProcessor: ReorderProcessor) {

    val repository = ReorderRepository()

    get("/reorders") {
        val reorders = repository.getReorders()
        call.respond(
            ApiResponse(
                data = reorders,
                error = null
            )
        )
    }

    get("/reorders/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        if (id == null) {
            call.respondValidationError("Invalid reorder id")
            return@get
        }
        val reorder = repository.getReorderById(id)

        if (reorder == null) {
            call.respondNotFoundError("Reorder not found")
            return@get
        }
        call.respond(
            ApiResponse(
                data = reorder,
                error = null
            )
        )
    }

    post("/reorders") {
        val request = call.receive<CreateReorderRequest>()

        if (request.quantity <= 0) {
            call.respondValidationError("Quantity must be greater than 0")
            return@post
        }
        if (!repository.itemExists(request.itemId)) {
            call.respondNotFoundError("Item not found")
            return@post
        }

        val id = repository.createReorder(request)

        if (id == null) {
            call.respondValidationError("Could not create reorder")
            return@post
        }

        reorderProcessor.enqueue(id)

        call.respond(
            HttpStatusCode.Created,
            ApiResponse(
                data = repository.getReorderById(id),
                error = null
            )
        )
    }
}
