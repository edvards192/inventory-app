package com.example.app.data.remote.dto

import com.example.app.domain.model.UserRole
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
