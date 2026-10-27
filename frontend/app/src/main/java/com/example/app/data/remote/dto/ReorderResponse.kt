package com.example.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReorderResponse(
    val id: Int,
    val itemId: Int,
    val itemTitle: String,
    val quantity: Int,
    val status: String,
    val createdAt: String,
    val completedAt: String? = null
)
