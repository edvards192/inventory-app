package com.example.models.responses

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val id: Int,
    val name: String,
    val surname: String,
    val email: String,
    val token: String
)
