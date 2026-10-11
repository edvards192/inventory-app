package com.example.models.requests

import kotlinx.serialization.Serializable

@Serializable
data class CreateItemRequest(
    val title: String,
    val ean: String,
    val description: String? = null,
    val price: Double? = null,
    val reorderPoint: Int = 0
)