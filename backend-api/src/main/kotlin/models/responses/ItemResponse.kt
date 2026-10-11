package com.example.models.responses

import kotlinx.serialization.Serializable

@Serializable
data class ItemResponse(
    val id: Int,
    val title: String,
    val ean: String,
    val description: String?,
    val price: Double?,
    val reorderPoint: Int,
    val createdAt: String,
    val updatedAt: String,
    val storageLocations: List<StorageResponse>,
    val images: List<ItemImageResponse>
)