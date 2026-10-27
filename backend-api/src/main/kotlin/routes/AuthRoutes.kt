package com.example.routes

import com.example.models.requests.LoginRequest
import com.example.models.responses.AuthResponse
import com.example.security.PasswordHasher
import com.example.security.JwtConfig
import com.example.models.responses.ApiResponse
import com.example.repositories.UserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Route.authRoutes() {

    val userRepository = UserRepository()

    post("/auth/login") {

        val request = call.receive<LoginRequest>()

        val user = userRepository.findByEmail(request.email)

        if (user == null) {
            call.respond(
                HttpStatusCode.Unauthorized,
                ApiResponse<Unit>(error = "Invalid email or password")
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
                ApiResponse<Unit>(error = "Invalid email or password")
            )
            return@post
        }
        val token = JwtConfig.generateToken(userId = user.id)

        call.respond(
            HttpStatusCode.OK,
            ApiResponse(
                data = AuthResponse(
                    id = user.id,
                    name = user.name,
                    surname = user.surname,
                    email = user.email,
                    token = token
                )
            )
        )
    }
}
