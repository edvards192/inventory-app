package com.example.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class StorageResponse(
    val warehouseId: Int,
    val warehouseCode: String,
    val warehouseName: String,
    val count: Int
)