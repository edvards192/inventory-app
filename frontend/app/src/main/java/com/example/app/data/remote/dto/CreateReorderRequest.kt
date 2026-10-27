package com.example.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateReorderRequest(
    val itemId: Int,
    val quantity: Int
)
