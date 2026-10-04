package com.example.app.data.remote.dto

import com.example.app.domain.model.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(
    val name: String,
    val surname: String,
    val email: String,
    val password: String,
    val role: UserRole
)

@Serializable
data class UpdateUserRequest(
    val name: String,
    val surname: String,
    val email: String,
    val role: UserRole,
    val isActive: Boolean
)

@Serializable
data class UpdateUserStatusRequest(val isActive: Boolean)
