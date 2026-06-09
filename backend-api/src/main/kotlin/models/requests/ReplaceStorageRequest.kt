package com.example.models.requests

import kotlinx.serialization.Serializable

@Serializable
data class ReplaceStorageRequest(
    val storageLocations: List<UpdateStorageRequest>
)