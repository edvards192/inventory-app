package com.example.plugins

import com.example.security.JwtConfig
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

                if (userId != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}
