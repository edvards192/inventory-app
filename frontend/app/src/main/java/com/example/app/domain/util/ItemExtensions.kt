package com.example.app.domain.util

import com.example.app.BuildConfig
import com.example.app.domain.model.Item
import com.example.app.domain.model.ItemImage

fun ItemImage.fullUrl(): String {
    return "${BuildConfig.API_BASE_URL.trimEnd('/')}/${url.trimStart('/')}"
}

fun Item.getListImage(): String? {
    return images
        .minByOrNull { it.sortOrder }
        ?.fullUrl()
}
