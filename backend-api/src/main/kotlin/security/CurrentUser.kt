package com.example.security

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal

fun ApplicationCall.userId(): Int {
    return principal<JWTPrincipal>()!!
        .payload
        .getClaim("userId")
        .asInt()
}
