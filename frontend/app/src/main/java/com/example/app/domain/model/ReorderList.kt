package com.example.app.domain.model

data class ReorderList(
    val queue: List<Reorder>,
    val history: List<Reorder>
)
