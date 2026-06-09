package com.example.app.domain.model

data class Item(
    val id: Int,
    val title: String,
    val ean: String,
    val storageLocations: List<Storage> = emptyList(),
    val images: List<ItemImage> = emptyList()
)