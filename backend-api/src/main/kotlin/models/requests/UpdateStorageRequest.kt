package com.example.models.requests

import kotlinx.serialization.Serializable

@Serializable
data class UpdateStorageRequest(
    val warehouseId: Int,
    val count: Int
)