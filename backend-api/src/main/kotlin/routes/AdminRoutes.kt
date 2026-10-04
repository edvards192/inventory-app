package com.example.routes

import com.example.models.requests.*
import io.ktor.server.request.receive

import com.example.models.responses.UserResponse
import com.example.models.UserRole
import com.example.models.responses.ApiResponse
import com.example.repositories.UserRepository
import com.example.security.hasRole
import com.example.validation.UserValidator
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.*

fun Route.adminRoutes() {

    val userRepository = UserRepository()

    authenticate("auth-jwt") {

        get("/admin/users") {
            val principal = call.principal<JWTPrincipal>()
            if (principal == null || !principal.hasRole(UserRole.ADMIN)) {
                return@get call.respond(
                    HttpStatusCode.Forbidden,
                    ApiResponse<Unit>(error = "Forbidden")
                )
            }

            val users = userRepository.getAllUsers().sortedBy { it.id }.map { user ->
                UserResponse(
                    id = user.id,
                    name = user.name,
                    surname = user.surname,
                    email = user.email,
                    role = user.role,
                    isActive = user.isActive,
                    createdAt = user.createdAt.toString()
                )
            }
            call.respond(ApiResponse(data = users))
        }

        post("/admin/users") {

            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse<Unit>(error = "Unauthorized")
                )

            if (!principal.hasRole(UserRole.ADMIN)) {
                return@post call.respond(
                    HttpStatusCode.Forbidden,
                    ApiResponse<Unit>(error = "Forbidden")
                )
            }

            val body = call.receive<CreateUserRequest>()
            val request = body.copy(name = body.name.trim(), surname = body.surname.trim(), email = body.email.trim())
            UserValidator.validate(request.name, request.surname, request.email, request.password)?.let {
                return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(error = it))
            }

            val existingUser = userRepository.findByEmail(request.email)

            if (existingUser != null) {
                return@post call.respond(
                    HttpStatusCode.Conflict,
                    ApiResponse<Unit>(error = "Email is already registered")
                )
            }

            val userId = userRepository.createUser(
                name = request.name,
                surname = request.surname,
                email = request.email,
                password = request.password,
                role = request.role
            )

            call.respond(
                HttpStatusCode.Created,
                ApiResponse(
                    data = mapOf("id" to userId)
                )
            )
        }
        put("/admin/users/{id}") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@put call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse<Unit>(error = "Unauthorized")
                )

            if (!principal.hasRole(UserRole.ADMIN)) {
                return@put call.respond(
                    HttpStatusCode.Forbidden,
                    ApiResponse<Unit>(error = "Forbidden")
                )
            }

            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(error = "Invalid user id")
                )

            val body = call.receive<UpdateUserRequest>()
            val request = body.copy(name = body.name.trim(), surname = body.surname.trim(), email = body.email.trim())
            UserValidator.validate(request.name, request.surname, request.email)?.let {
                return@put call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(error = it))
            }

            val currentUserId = principal.payload
                .getClaim("userId")
                .asInt()

            if (currentUserId == id && request.role != UserRole.ADMIN) {
                return@put call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(error = "You cannot remove your own admin role")
                )
            }

            if (currentUserId == id && !request.isActive) {
                return@put call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(error = "You cannot deactivate your own account")
                )
            }

            val existingEmailUser = userRepository.findByEmail(request.email)

            if (existingEmailUser != null && existingEmailUser.id != id) {
                return@put call.respond(
                    HttpStatusCode.Conflict,
                    ApiResponse<Unit>(error = "Email is already registered")
                )
            }

            val updated = userRepository.updateUser(
                id = id,
                name = request.name,
                surname = request.surname,
                email = request.email,
                role = request.role,
                isActive = request.isActive
            )

            if (!updated) {
                return@put call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse<Unit>(error = "User not found")
                )
            }

            call.respond(
                HttpStatusCode.OK,
                ApiResponse(
                    data = mapOf("status" to "updated")
                )
            )
        }

        patch("/admin/users/{id}/status") {
        val principal = call.principal<JWTPrincipal>()
            ?: return@patch call.respond(
                HttpStatusCode.Unauthorized,
                ApiResponse<Unit>(error = "Unauthorized")
            )

        if (!principal.hasRole(UserRole.ADMIN)) {
            return@patch call.respond(
                HttpStatusCode.Forbidden,
                ApiResponse<Unit>(error = "Forbidden")
            )
        }

        val id = call.parameters["id"]?.toIntOrNull()
            ?: return@patch call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Unit>(error = "Invalid user id")
            )

        val request = call.receive<UpdateUserStatusRequest>()

        val currentUserId = principal.payload
            .getClaim("userId")
            .asInt()

        if (currentUserId == id && !request.isActive) {
            return@patch call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Unit>(error = "You cannot deactivate your own account")
            )
        }

        val updated = userRepository.setUserActiveStatus(
            id = id,
            isActive = request.isActive
        )

        if (!updated) {
            return@patch call.respond(
                HttpStatusCode.NotFound,
                ApiResponse<Unit>(error = "User not found")
            )
        }

        call.respond(
            HttpStatusCode.OK,
            ApiResponse(
                data = mapOf("status" to "updated")
            )
        )
        }
        delete("/admin/users/{id}") {
            val principal = call.principal<JWTPrincipal>()
                ?: return@delete call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse<Unit>(error = "Unauthorized")
                )

            if (!principal.hasRole(UserRole.ADMIN)) {
                return@delete call.respond(
                    HttpStatusCode.Forbidden,
                    ApiResponse<Unit>(error = "Forbidden")
                )
            }

            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(error = "Invalid user id")
                )
                
            val currentUserId = principal.payload
                .getClaim("userId")
                .asInt()
            if (currentUserId == id) {
                return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Unit>(
                        error = "You cannot delete your own account"
                    )
                )
            }

            val deleted = userRepository.deleteUser(id)

            if (!deleted) {
                return@delete call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse<Unit>(error = "User not found")
                )
            }

            call.respond(
                HttpStatusCode.OK,
                ApiResponse(
                    data = mapOf("status" to "deleted")
                )
            )
        }
    }
}
