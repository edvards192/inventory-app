package com.example.models.requests

import kotlinx.serialization.Serializable

@Serializable
data class CreateItemRequest(
    val title: String,
    val ean: String
)