package com.example.app.data.remote.dto

@kotlinx.serialization.Serializable
data class ItemImageResponse(
    val id: Int,
    val url: String,
    val sortOrder: Int
)