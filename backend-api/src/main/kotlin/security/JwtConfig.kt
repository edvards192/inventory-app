package com.example.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtConfig {

    const val SECRET = "temporary-development-secret"
    const val ISSUER = "inventory-backend"
    const val AUDIENCE = "inventory-users"

    private const val EXPIRATION_TIME = 7L * 24 * 60 * 60 * 1000

    val algorithm = Algorithm.HMAC256(SECRET)

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
            .sign(algorithm)
    }
}
