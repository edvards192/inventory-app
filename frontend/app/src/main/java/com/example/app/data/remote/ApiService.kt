package com.example.app.data.remote

import com.example.app.data.remote.dto.ItemResponse
import retrofit2.http.GET
import com.example.app.data.remote.dto.ApiResponse
import retrofit2.http.POST
import retrofit2.http.Body
import com.example.app.data.remote.dto.CreateItemRequest
import com.example.app.data.remote.dto.UpdateItemRequest
import com.example.app.data.remote.dto.WarehouseResponse
import com.example.app.data.remote.dto.ReplaceStorageRequest
import retrofit2.http.Path
import retrofit2.http.DELETE
import retrofit2.http.PUT
import okhttp3.MultipartBody
import retrofit2.http.Part
import retrofit2.http.Multipart
import retrofit2.http.Query


interface ApiService {
    @GET("items")
    suspend fun getItems(
        @Query("search")
        search: String = "",
        @Query("sort")
        sort: String = "updatedDesc",
        @Query("stockFilter")
        stockFilter: String = "all",
        @Query("warehouseIds")
        warehouseIds: String = "",
        @Query("minQuantity")
        minQuantity: Int? = null,
        @Query("maxQuantity")
        maxQuantity: Int? = null,
        @Query("page")
        page: Int = 1,
        @Query("limit")
        limit: Int = 25
    ): ApiResponse<List<ItemResponse>>

    @POST("items")
    suspend fun createItem(
        @Body request: CreateItemRequest
    ): ApiResponse<Map<String, Int>>

    @GET("items/{id}")
    suspend fun getItemById(
        @Path("id") id: Int
    ): ApiResponse<ItemResponse>

    @DELETE("items/{id}")
    suspend fun deleteItem(
        @Path("id") id: Int
    ): ApiResponse<Unit>

    @PUT("items/{id}")
    suspend fun updateItem(
        @Path("id") id: Int,
        @Body request: UpdateItemRequest
    ): ApiResponse<Map<String, String>>

    @Multipart
    @POST("items/{id}/image")
    suspend fun uploadImage(
        @Path("id") itemId: Int,
        @Part("sortOrder") sortOrder: Int,
        @Part file: MultipartBody.Part
    ): ApiResponse<Map<String, String>>

    @DELETE("images/{id}")
    suspend fun deleteImage(
        @Path("id") imageId: Int
    ): ApiResponse<Map<String, String>>

    @GET("warehouses")
    suspend fun getWarehouses():
            ApiResponse<List<WarehouseResponse>>

    @PUT("items/{id}/storage")
    suspend fun replaceStorage(
        @Path("id")
        itemId: Int,
        @Body
        request: ReplaceStorageRequest
    ): ApiResponse<Map<String, String>>

    @GET("items/ean/{ean}")
    suspend fun getItemByEan(
        @Path("ean") ean: String
    ): ApiResponse<ItemResponse>

}
