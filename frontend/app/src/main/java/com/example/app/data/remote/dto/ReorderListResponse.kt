package com.example.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReorderListResponse(
    val queue: List<ReorderResponse> = emptyList(),
    val history: List<ReorderResponse> = emptyList()
)
