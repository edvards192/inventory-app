package com.example.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class WarehouseResponse(
    val id: Int,
    val code: String,
    val name: String
)