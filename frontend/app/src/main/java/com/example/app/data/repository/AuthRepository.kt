package com.example.app.data.repository

import com.example.app.data.local.AuthDataStore
import com.example.app.data.remote.RetrofitClient
import com.example.app.data.remote.dto.ApiResponse
import com.example.app.data.remote.dto.auth.AuthResponse
import com.example.app.data.remote.dto.auth.LoginRequest
import com.example.app.data.remote.dto.auth.RegisterRequest

class AuthRepository(
    private val authDataStore: AuthDataStore
) {

    suspend fun login(
        email: String,
        password: String
    ): ApiResponse<AuthResponse> {

        val response = RetrofitClient.api.login(
            LoginRequest(
                email = email,
                password = password
            )
        )

        response.data?.let { auth ->
            authDataStore.saveSession(
                token = auth.token,
                role = auth.role.name,
                userId = auth.id,
                name = auth.name,
                surname = auth.surname,
                email = auth.email
            )
        }

        return response
    }

    suspend fun register(
        name: String,
        surname: String,
        email: String,
        password: String
    ): ApiResponse<AuthResponse> {

        val response = RetrofitClient.api.register(
            RegisterRequest(
                name = name,
                surname = surname,
                email = email,
                password = password
            )
        )

        response.data?.let { auth ->
            authDataStore.saveSession(
                token = auth.token,
                role = auth.role.name,
                userId = auth.id,
                name = auth.name,
                surname = auth.surname,
                email = auth.email
            )
        }

        return response
    }

    suspend fun logout() {
        authDataStore.clearSession()
    }
}
