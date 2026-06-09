package com.example.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReplaceStorageRequest(
    val storageLocations: List<UpdateStorageRequest>
)