package com.example.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateItemImageRequest(
    val url: String,
    val sortOrder: Int
)