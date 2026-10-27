package com.example.app.domain.model

data class Reorder(
    val id: Int,
    val itemId: Int,
    val itemTitle: String,
    val quantity: Int,
    val status: ReorderStatus,
    val createdAt: String,
    val completedAt: String?
)
