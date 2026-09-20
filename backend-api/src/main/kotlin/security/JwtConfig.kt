package com.example.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtConfig {

    private const val SECRET = "temporary-development-secret"
    private const val ISSUER = "app-backend"
    private const val AUDIENCE = "app-client"

    private const val EXPIRATION_TIME = 7L * 24 * 60 * 60 * 1000

    fun generateToken(
        userId: Int,
        role: String
    ): String {
        return JWT.create()
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .withClaim("userId", userId)
            .withClaim("role", role)
            .withExpiresAt(
                Date(System.currentTimeMillis() + EXPIRATION_TIME)
            )
            .sign(Algorithm.HMAC256(SECRET))
    }
}
