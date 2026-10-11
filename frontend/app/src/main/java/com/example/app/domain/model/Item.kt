package com.example.app.domain.model

data class Item(
    val id: Int,
    val title: String,
    val ean: String,
    val description: String? = null,
    val price: Double? = null,
    val reorderPoint: Int = 0,
    val storageLocations: List<Storage> = emptyList(),
    val images: List<ItemImage> = emptyList()
)