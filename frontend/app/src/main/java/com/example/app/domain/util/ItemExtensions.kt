package com.example.app.domain.util

import com.example.app.domain.model.Item
import com.example.app.domain.model.ItemImage

private const val BASE_URL = "http://192.168.101.4:8080"

fun ItemImage.fullUrl(): String {
    return "$BASE_URL$url"
}

fun Item.getListImage(): String? {
    return images
        .minByOrNull { it.sortOrder }
        ?.fullUrl()
}