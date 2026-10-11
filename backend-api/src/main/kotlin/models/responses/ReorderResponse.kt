package com.example.models.responses

import com.example.models.ReorderStatus
import kotlinx.serialization.Serializable

@Serializable
data class ReorderResponse(
    val id: Int,
    val itemId: Int,
    val itemTitle: String,
    val quantity: Int,
    val status: ReorderStatus,
    val createdAt: String,
    val completedAt: String?
)
