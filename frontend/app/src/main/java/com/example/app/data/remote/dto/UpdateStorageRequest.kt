package com.example.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateStorageRequest(
    val warehouseId: Int,
    val count: Int
)