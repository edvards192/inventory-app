package com.example.models.requests

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserStatusRequest(
    val isActive: Boolean
)