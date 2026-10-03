package com.example.models.requests

import kotlinx.serialization.Serializable
import com.example.models.UserRole

@Serializable
data class CreateUserRequest(
    val name: String,
    val surname: String,
    val email: String,
    val password: String,
    val role: UserRole
)
