package com.example.plugins

import com.example.security.JwtConfig
import com.example.repositories.UserRepository
import com.auth0.jwt.JWT
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureAuthentication() {

    install(Authentication) {

        jwt("auth-jwt") {

            realm = "inventory-app"

            verifier(
                JWT
                    .require(JwtConfig.algorithm)
                    .withAudience(JwtConfig.AUDIENCE)
                    .withIssuer(JwtConfig.ISSUER)
                    .build()
            )

            validate { credential ->

                val userId = credential.payload
                    .getClaim("userId")
                    .asInt()

                val user = userId?.let { UserRepository().findById(it) }

                if (user != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}
