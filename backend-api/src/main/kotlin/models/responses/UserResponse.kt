package com.example.models.responses

import com.example.models.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: Int,
    val name: String,
    val surname: String,
    val email: String,
    val role: UserRole,
    val isActive: Boolean,
    val createdAt: String
)
