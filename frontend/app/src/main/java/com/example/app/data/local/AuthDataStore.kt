package com.example.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "auth_preferences"
)

class AuthDataStore(
    private val context: Context
) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val ROLE_KEY = stringPreferencesKey("role")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    suspend fun saveSession(
        token: String,
        role: String,
        userId: Int
    ) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[ROLE_KEY] = role
            preferences[USER_ID_KEY] = userId.toString()
        }
    }

    val token: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }

    val role: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[ROLE_KEY]
        }

    val userId: Flow<Int?> =
        context.dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]?.toIntOrNull()
        }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(ROLE_KEY)
            preferences.remove(USER_ID_KEY)
        }
    }
}