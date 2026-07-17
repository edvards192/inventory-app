package com.example.routes

import com.example.repositories.ItemRepository
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import com.example.models.requests.CreateItemRequest
import io.ktor.server.request.*
import com.example.models.responses.ApiResponse
import com.example.validation.ItemValidator
import com.example.validation.respondNotFoundError
import com.example.validation.respondValidationError
import io.ktor.http.content.*
import java.io.File
import java.util.UUID
import com.example.models.requests.UpdateStorageRequest
import com.example.models.requests.ReplaceStorageRequest

fun Route.itemRoutes() {

    val repository = ItemRepository()

    get("/items") {
        val search =
            call.request
                .queryParameters["search"]
                ?: ""
        val sort =
            call.request
                .queryParameters["sort"]
                ?: "updatedDesc"
        val stockFilter =
            call.request
                .queryParameters["stockFilter"]
                ?: "all"
        val warehouseIds =
            call.request
                .queryParameters["warehouseIds"]
                ?.split(",")
                ?.mapNotNull {
                    it.toIntOrNull()
                }
                ?: emptyList()
        val minQuantity =
            call.request
                .queryParameters["minQuantity"]
                ?.toIntOrNull()
        val maxQuantity =
            call.request
                .queryParameters["maxQuantity"]
                ?.toIntOrNull()
        val page =
            call.request
                .queryParameters["page"]
                ?.toIntOrNull()
                ?: 1
        val limit =
            call.request
                .queryParameters["limit"]
                ?.toIntOrNull()
                ?: 25
        val items =
            repository.getAllItems(
                search,
                sort,
                stockFilter,
                warehouseIds,
                minQuantity,
                maxQuantity,
                page,
                limit
            )
        call.respond(
            ApiResponse(
                data = items,
                error = null
            )
        )
    }

    get("/warehouses") {
        val warehouses = repository.getWarehouses()
        call.respond(
            ApiResponse(
                data = warehouses,
                error = null
            )
        )
    }
    get("/items/ean/{ean}") {
        val ean = call.parameters["ean"]
        if (ean == null) {
            call.respondValidationError("Invalid EAN")
            return@get
        }
        val item = repository.getItemByEan(ean)

        if (item == null) {
            call.respond(
                ApiResponse(
                    data = null,
                    error = null
                )
            )
            return@get
        }
        call.respond(
            ApiResponse(
                data = item,
                error = null
            )
        )
    }
    get("/items/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        if (id == null) {
            call.respondValidationError("Invalid item id")
            return@get
        }
        val item = repository.getItemById(id)

        if (item == null) {
            call.respondNotFoundError("Item not found")
            return@get
        }
        call.respond(
            ApiResponse(
                data = item,
                error = null
            )
        )
    }

    post("/items") { // HTTP 201 response
        val request = call.receive<CreateItemRequest>()
        val validation = ItemValidator.validateCreate(request)
        if (!validation.isValid) {
            call.respondValidationError(validation.error!!)
            return@post
        }
        val id = repository.createItem(request)

        call.respond(
            HttpStatusCode.Created,
            ApiResponse(
                data = mapOf("id" to id),
                error = null
            )
        )
    }
    put("/items/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()

        if (id == null) {
            call.respondValidationError("Invalid item id")
            return@put
        }

        val request = call.receive<CreateItemRequest>()
        val validation = ItemValidator.validateUpdate(request)

        if (!validation.isValid) {
            call.respondValidationError(validation.error!!)
            return@put
        }

        val updated = repository.updateItem(id, request)

        if (!updated) {
            call.respondNotFoundError("Item not found")
            return@put
        }
        call.respond(
            HttpStatusCode.OK,
            ApiResponse(
                data = mapOf("status" to "updated"),
                error = null
            )
        )
    }

    delete("/items/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()

        if (id == null) {
            call.respondValidationError("Invalid item id")
            return@delete
        }
        val deleted = repository.deleteItem(id)

        if (!deleted) {
            call.respondNotFoundError("Item not found")
            return@delete
        }
        call.respond(
            HttpStatusCode.OK,
            ApiResponse(
                data = mapOf("status" to "deleted"),
                error = null
            )
        )
    }

    post("/items/{id}/storage") {
        val itemId = call.parameters["id"]?.toIntOrNull()
        if (itemId == null) {
            call.respondValidationError(
                "Invalid item id"
            )
            return@post
        }
        val request =
            call.receive<UpdateStorageRequest>()

        repository.addOrUpdateStorage(
            itemId = itemId,
            warehouseId = request.warehouseId,
            count = request.count
        )
        call.respond(
            ApiResponse(
                data = mapOf(
                    "status" to "updated"
                ),
                error = null
            )
        )
    }
    put("/items/{id}/storage") {
        val itemId =
            call.parameters["id"]?.toIntOrNull()

        if (itemId == null) {

            call.respondValidationError(
                "Invalid item id"
            )

            return@put
        }
        val request =
            call.receive<ReplaceStorageRequest>()

        repository.replaceStorage(
            itemId = itemId,
            request = request
        )
        call.respond(

            ApiResponse(

                data = mapOf(
                    "status" to "replaced"
                ),
                error = null
            )
        )
    }
}
