package com.example.models.responses

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val error: String
)