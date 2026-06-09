package com.example.routes

import com.example.models.responses.ApiResponse
import com.example.models.responses.UploadResponse
import com.example.repositories.ItemRepository
import com.example.services.FileStorageService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray

fun Route.uploadRoutes() {

    val repository = ItemRepository()
    val fileStorageService = FileStorageService()

    post("/items/{id}/image") {
        call.application.log.info("Image upload route hit: ${call.request.httpMethod.value} ${call.request.uri}")

        // 1. Parse item ID
        val itemId = call.parameters["id"]?.toIntOrNull()
        call.application.log.info("Parsed upload itemId=$itemId")

        if (itemId == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Unit>(
                    data = null,
                    error = "Invalid item id"
                )
            )
            return@post
        }

        // 2. Receive multipart data
        val multipart = call.receiveMultipart()

        var uploadedUrl: String? = null
        var sortOrder = 0

        // 3. Process file parts
        multipart.forEachPart { part ->

                when (part) {

                    is PartData.FormItem -> {

                        if (part.name == "sortOrder") {
                            sortOrder = part.value.toIntOrNull() ?: 0
                        }
                    }

                    is PartData.FileItem -> {

                        val fileName = part.originalFileName ?: "file.jpg"

                        call.application.log.info(
                            "Received upload file part: originalFileName=$fileName"
                        )

                        val bytes = part
                            .provider()
                            .readRemaining()
                            .readByteArray()

                        uploadedUrl = fileStorageService.saveFile(
                            bytes = bytes,
                            originalFileName = fileName
                        )
                    }

                    else -> Unit
                }

            part.dispose()
        }

        // 4. Validate upload result
        if (uploadedUrl == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Unit>(
                    data = null,
                    error = "No image uploaded"
                )
            )
            return@post
        }

        // 5. Save to database
        val imageId = try {
            call.application.log.info("Saving image URL to database: itemId=$itemId, uploadedUrl=$uploadedUrl, sortOrder = $sortOrder")

            repository.addImage(
                itemId = itemId,
                imageUrl = uploadedUrl,
                sortOrder = sortOrder
            )
        } catch (cause: Throwable) {
            call.application.log.error(
                "Failed to save image URL to database: itemId=$itemId, uploadedUrl=$uploadedUrl",
                cause
            )
            throw cause
        }

        call.application.log.info("Image URL saved to database: imageId=$imageId, itemId=$itemId")

        // 6. Response
        call.respond(
            HttpStatusCode.Created,
            ApiResponse(
                data = UploadResponse(
                    imageUrl = uploadedUrl
                ),
                error = null
            )
        )
    }
    delete("/images/{id}") {

    val imageId =
        call.parameters["id"]?.toIntOrNull()

    if (imageId == null) {
        call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(
                data = null,
                error = "Invalid image id"
            )
        )
        return@delete
    }

    val deleted =
        repository.deleteImage(imageId)

    if (!deleted) {
        call.respond(
            HttpStatusCode.NotFound,
            ApiResponse<Unit>(
                data = null,
                error = "Image not found"
            )
        )
        return@delete
    }

    call.respond(
        ApiResponse(
            data = mapOf(
                "status" to "deleted"
            ),
            error = null
        )
    )
}

}
