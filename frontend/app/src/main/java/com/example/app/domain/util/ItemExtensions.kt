package com.example.app.domain.util

import com.example.app.domain.model.Item
import com.example.app.domain.model.ItemImage

private const val BASE_URL = "http://10.160.63.98:8080"

fun ItemImage.fullUrl(): String {
    return "$BASE_URL$url"
}

fun Item.getListImage(): String? {
    return images
        .minByOrNull { it.sortOrder }
        ?.fullUrl()
}