package com.example.app.data.repository

import com.example.app.data.remote.ApiService
import com.example.app.data.remote.RetrofitClient
import com.example.app.data.remote.dto.ApiResponse
import com.example.app.data.remote.dto.CreateUserRequest
import com.example.app.data.remote.dto.UpdateUserRequest
import com.example.app.data.remote.dto.UpdateUserStatusRequest
import com.example.app.data.remote.dto.UserResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import java.io.IOException

class UserRepository(private val api: ApiService = RetrofitClient.api) {
    suspend fun getUsers(): List<UserResponse> = request { api.getUsers() }

    suspend fun createUser(user: CreateUserRequest) { request { api.createUser(user) } }

    suspend fun updateUser(id: Int, user: UpdateUserRequest) { request { api.updateUser(id, user) } }

    suspend fun setActive(id: Int, active: Boolean) {
        request { api.updateUserStatus(id, UpdateUserStatusRequest(active)) }
    }

    suspend fun deleteUser(id: Int) { request { api.deleteUser(id) } }

    private suspend fun <T> request(block: suspend () -> ApiResponse<T>): T {
        val response = try {
            block()
        } catch (e: HttpException) {
            val message = runCatching {
                e.response()?.errorBody()?.string()?.let {
                    Json.parseToJsonElement(it).jsonObject["error"]?.jsonPrimitive?.content
                }
            }.getOrNull()
            throw UserManagementException(message ?: when (e.code()) {
                401 -> "Your session has expired. Please log in again."
                403 -> "Only administrators can manage users."
                else -> "Unable to complete the request. Please try again."
            })
        } catch (e: IOException) {
            throw UserManagementException("Cannot connect to the server. Please try again.")
        }
        response.error?.let { throw UserManagementException(it) }
        return response.data ?: throw UserManagementException("The server returned an empty response.")
    }
}

class UserManagementException(message: String) : Exception(message)
