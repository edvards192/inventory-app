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
        private val NAME_KEY = stringPreferencesKey("name")
        private val SURNAME_KEY = stringPreferencesKey("surname")
        private val EMAIL_KEY = stringPreferencesKey("email")
    }

    suspend fun saveSession(
        token: String,
        role: String,
        userId: Int,
        name: String,
        surname: String,
        email: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[ROLE_KEY] = role
            preferences[USER_ID_KEY] = userId.toString()
            preferences[NAME_KEY] = name
            preferences[SURNAME_KEY] = surname
            preferences[EMAIL_KEY] = email
        }
    }

    val session: Flow<AuthSessionData> =
        context.dataStore.data.map { preferences ->
            AuthSessionData(
                token = preferences[TOKEN_KEY],
                role = preferences[ROLE_KEY],
                userId = preferences[USER_ID_KEY]?.toIntOrNull(),
                name = preferences[NAME_KEY],
                surname = preferences[SURNAME_KEY],
                email = preferences[EMAIL_KEY]
            )
        }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(ROLE_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(NAME_KEY)
            preferences.remove(SURNAME_KEY)
            preferences.remove(EMAIL_KEY)
        }
    }
}

data class AuthSessionData(
    val token: String?,
    val role: String?,
    val userId: Int?,
    val name: String?,
    val surname: String?,
    val email: String?
)
