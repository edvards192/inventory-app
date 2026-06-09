package com.example.app.data.repository

import com.example.app.data.mapper.toDomain
import com.example.app.data.remote.RetrofitClient
import com.example.app.domain.model.Item
import com.example.app.data.remote.dto.CreateItemRequest
import com.example.app.data.remote.dto.UpdateItemRequest
import com.example.app.data.remote.dto.ReplaceStorageRequest
import com.example.app.data.remote.dto.UpdateStorageRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import com.example.app.data.remote.dto.CreateItemImageRequest
import com.example.app.domain.model.Warehouse
import com.example.app.domain.model.Storage


class ItemRepository {
    suspend fun getItems(search: String = ""): List<Item> {
        return RetrofitClient.api
            .getItems(search)
            .data
            ?.map {
                it.toDomain()
            }
            ?: emptyList()
    }

    suspend fun getWarehouses(): List<Warehouse> {
        return RetrofitClient.api.getWarehouses().data
            ?.map { it.toDomain() }
            ?: emptyList()
    }

    suspend fun createItem(item: Item): CreateItemResult {

        val request = CreateItemRequest(
            title = item.title,
            ean = item.ean,
            images = item.images.map {
                CreateItemImageRequest(
                    url = it.url,
                    sortOrder = it.sortOrder
                )
            }
        )

        val response = RetrofitClient.api.createItem(request)

        return CreateItemResult(
            success = response.error == null,
            itemId = response.data?.get("id")
        )
    }

    suspend fun getItemById(id: Int): Item {
        val response = RetrofitClient.api.getItemById(id)

        return response.data!!.toDomain()
    }
    suspend fun deleteItem(id: Int): Boolean {
        val response = RetrofitClient.api.deleteItem(id)
        return response.error == null
    }
    suspend fun updateItem(id: Int, item: Item): Boolean {

        val request = UpdateItemRequest(
            title = item.title,
            ean = item.ean
        )

        val response = RetrofitClient.api.updateItem(id, request)

        return response.error == null
    }
    suspend fun uploadImage(itemId: Int, sortOrder: Int, file: File): Boolean {
        return try {

            val requestFile = file.asRequestBody(
                "image/*".toMediaTypeOrNull()
            )

            val body = MultipartBody.Part.createFormData(
                "file",
                file.name,
                requestFile
            )

            val response = RetrofitClient.api.uploadImage(
                itemId = itemId,
                sortOrder = sortOrder,
                file = body
            )

            response.error == null

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    suspend fun deleteImage(imageId: Int): Boolean {
        val response =
            RetrofitClient.api.deleteImage(imageId)

        return response.error == null
    }
    suspend fun replaceStorage(
        itemId: Int,
        storage: List<Storage>
    ): Boolean {

        val request =
            ReplaceStorageRequest(

                storageLocations =
                    storage.map {

                        UpdateStorageRequest(
                            warehouseId =
                                it.warehouseId,

                            count =
                                it.count
                        )
                    }
            )

        val response =
            RetrofitClient.api.replaceStorage(
                itemId,
                request
            )

        return response.error == null
    }
    suspend fun getItemByEan(ean: String): Item? {
        val response =
            RetrofitClient.api
                .getItemByEan(ean)
        return response.data?.toDomain()
    }
}