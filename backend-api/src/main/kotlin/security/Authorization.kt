package com.example.security

import com.example.models.UserRole
import io.ktor.server.auth.jwt.JWTPrincipal


fun JWTPrincipal.hasRole(role: UserRole): Boolean {
    val userRole = payload
        .getClaim("role")
        .asString()

    return userRole == role.name
}
