package com.example.models.responses

import kotlinx.serialization.Serializable

@Serializable
data class WarehouseResponse(
    val id: Int,
    val code: String,
    val name: String
)