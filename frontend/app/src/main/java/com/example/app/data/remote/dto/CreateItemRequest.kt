package com.example.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateItemRequest(
    val title: String,
    val ean: String,
    val images: List<CreateItemImageRequest> = emptyList()
)