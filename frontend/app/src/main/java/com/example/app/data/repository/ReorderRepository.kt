package com.example.app.data.repository

import com.example.app.data.mapper.toDomain
import com.example.app.data.remote.RetrofitClient
import com.example.app.data.remote.dto.CreateReorderRequest
import com.example.app.domain.model.ReorderList

class ReorderRepository {

    suspend fun getReorders(): ReorderList {
        return RetrofitClient.api.getReorders().data
            ?.toDomain()
            ?: ReorderList(
                queue = emptyList(),
                history = emptyList()
            )
    }

    suspend fun createReorder(itemId: Int, quantity: Int): Boolean {
        val request = CreateReorderRequest(
            itemId = itemId,
            quantity = quantity
        )
        val response = RetrofitClient.api.createReorder(request)
        return response.error == null
    }
}
