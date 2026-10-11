package com.example.models.requests

import kotlinx.serialization.Serializable

@Serializable
data class CreateReorderRequest(
    val itemId: Int,
    val quantity: Int
)
