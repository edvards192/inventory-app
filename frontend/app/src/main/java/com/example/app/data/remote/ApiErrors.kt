package com.example.app.data.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import java.io.IOException

fun Throwable.toUserMessage(fallback: String): String {
    return when (this) {
        is HttpException -> errorBodyMessage() ?: fallback
        is IOException -> "Cannot connect to the server. Please try again."
        else -> fallback
    }
}

private fun HttpException.errorBodyMessage(): String? {
    return runCatching {
        response()?.errorBody()?.string()?.let { body ->
            Json.parseToJsonElement(body).jsonObject["error"]?.jsonPrimitive?.content
        }
    }.getOrNull()
}
