package com.example.models.responses

import kotlinx.serialization.Serializable

@Serializable
data class StorageResponse(
    val warehouseId: Int,
    val warehouseCode: String,
    val warehouseName: String,
    val count: Int
)