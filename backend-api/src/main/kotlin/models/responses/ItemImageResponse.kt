package com.example.models.responses

import kotlinx.serialization.Serializable

@Serializable
data class ItemImageResponse(
    val id: Int,
    val url: String,
    val sortOrder: Int
)