package com.example.routes

import com.example.models.requests.LoginRequest
import com.example.models.responses.AuthResponse
import com.example.models.UserRole
import com.example.models.requests.RegisterRequest
import com.example.security.hasRole
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

        if (user == null || !user.isActive) {
            call.respond(
                HttpStatusCode.Unauthorized,
                ApiResponse<Unit>(error = "Invalid email or password")
            )
            return@post
        }
        if (!user.isActive) {
            call.respond(
                HttpStatusCode.Forbidden,
                ApiResponse<Unit>(error = "Account is inactive")
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
        val token = JwtConfig.generateToken( userId = user.id, role = user.role.name)

        call.respond(
            HttpStatusCode.OK,
            ApiResponse(
                data = AuthResponse(
                    id = user.id,
                    name = user.name,
                    surname = user.surname,
                    email = user.email,
                    role = user.role,
                    token = token
                )
            )
        )
    }
    post("/auth/register") {
        val request = call.receive<RegisterRequest>()

        val existingUser = userRepository.findByEmail(request.email)

        if (existingUser != null) {
            call.respond(
                HttpStatusCode.Conflict,
                ApiResponse<Unit>(
                    error = "Email is already registered"
                )
            )
            return@post
        }

        val userId = userRepository.createUser(
            name = request.name,
            surname = request.surname,
            email = request.email,
            password = request.password,
            role = UserRole.VIEW
        )

        val token = JwtConfig.generateToken(
            userId = userId,
            role = UserRole.VIEW.name
        )

        call.respond(
            HttpStatusCode.Created,
            ApiResponse(
                    data = AuthResponse(
                        id = userId,
                        name = request.name,
                        surname = request.surname,
                        email = request.email,
                        role = UserRole.VIEW,
                        token = token
                        )
                    )
        )   
    }
}
