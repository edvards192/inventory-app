package com.example.models.responses

import kotlinx.serialization.Serializable

@Serializable
data class ReorderListResponse(
    val queue: List<ReorderResponse>,
    val history: List<ReorderResponse>
)
