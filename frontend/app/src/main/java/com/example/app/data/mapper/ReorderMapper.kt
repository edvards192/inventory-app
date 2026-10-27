package com.example.app.data.mapper

import com.example.app.data.remote.dto.ReorderListResponse
import com.example.app.data.remote.dto.ReorderResponse
import com.example.app.domain.model.Reorder
import com.example.app.domain.model.ReorderList
import com.example.app.domain.model.ReorderStatus

fun ReorderResponse.toDomain() = Reorder(
    id = id,
    itemId = itemId,
    itemTitle = itemTitle,
    quantity = quantity,
    status = ReorderStatus.valueOf(status),
    createdAt = createdAt,
    completedAt = completedAt
)

fun ReorderListResponse.toDomain() = ReorderList(
    queue = queue.map { it.toDomain() },
    history = history.map { it.toDomain() }
)
