package com.example.models.requests

import com.example.models.UserRole

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRequest(
    val name: String,
    val surname: String,
    val email: String,
    val role: UserRole,
    val isActive: Boolean
)
