package com.example.routes

import LoginRequest
import LoginResponse
import PasswordHasher
import com.example.models.responses.ApiResponse
import com.example.repositories.UserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.authRoutes() {

    val userRepository = UserRepository()

    post("/auth/login") {

        val request = call.receive<LoginRequest>()

        val user = userRepository.findByUsername(request.username)

        if (user == null) {
            call.respond(
                HttpStatusCode.Unauthorized,
                ApiResponse<Unit>(error = "Invalid username or password")
            )
            return@post
        }

        val passwordCorrect = PasswordHasher.verify(
            request.password,
            user.passwordHash
        )

        if (!passwordCorrect) {
            call.respond(
                HttpStatusCode.Unauthorized,
                ApiResponse<Unit>(error = "Invalid username or password")
            )
            return@post
        }

        call.respond(
            HttpStatusCode.OK,
            ApiResponse(
                data = LoginResponse(
                    id = user.id,
                    username = user.username,
                    role = user.role
                )
            )
        )
    }
}