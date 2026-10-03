package com.example.app.data.remote

import android.content.Context
import com.example.app.data.local.AuthDataStore
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitClient {

    //private const val BASE_URL = "http://10.0.2.2:8080/"
    private const val BASE_URL = "http://192.168.101.4:8080/"
    //private const val BASE_URL = "http://10.160.63.98:8080/"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private lateinit var client: OkHttpClient

    fun initialize(context: Context) {
        val authDataStore = AuthDataStore(
            context.applicationContext
        )

        client = OkHttpClient.Builder()
            .addInterceptor(
                AuthInterceptor(authDataStore)
            )
            .addInterceptor(loggingInterceptor)
            .build()
    }

    val api: ApiService by lazy {

        check(::client.isInitialized) {
            "RetrofitClient.initialize(context) must be called first"
        }

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(
                json.asConverterFactory(
                    "application/json".toMediaType()
                )
            )
            .build()
            .create(ApiService::class.java)
    }
}