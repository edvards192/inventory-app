package com.example.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateItemRequest(
    val title: String,
    val ean: String
)