package com.example.models.responses

import kotlinx.serialization.Serializable

@Serializable
data class UploadResponse(
    val imageUrl: String
)