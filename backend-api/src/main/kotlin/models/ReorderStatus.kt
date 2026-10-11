package com.example.models

import kotlinx.serialization.Serializable

@Serializable
enum class ReorderStatus {
    WAITING,
    PROCESSING,
    COMPLETED
}
