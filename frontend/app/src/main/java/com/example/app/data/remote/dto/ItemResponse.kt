package com.example.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ItemResponse(
    val id: Int,
    val title: String,
    val ean: String,
    val description: String? = null,
    val price: Double? = null,
    val reorderPoint: Int = 0,
    val storageLocations: List<StorageResponse> = emptyList(),
    val images: List<ItemImageResponse> = emptyList()
)