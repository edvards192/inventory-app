package com.example.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app.data.local.AuthDataStore
import com.example.app.data.repository.AuthRepository
import com.example.app.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val userId: Int? = null,
    val role: UserRole? = null,
    val error: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository,
    private val authDataStore: AuthDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        observeSession()
    }

    fun login(
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )
            try {
                val response = repository.login(
                    email = email,
                    password = password
                )
                val auth = response.data
                if (auth != null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        userId = auth.id,
                        role = auth.role,
                        error = null
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = response.error ?: "Login failed"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Login failed"
                )
            }
        }
    }

    fun register(
        name: String,
        surname: String,
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )
            try {
                val response = repository.register(
                    name = name,
                    surname = surname,
                    email = email,
                    password = password
                )
                val auth = response.data
                if (auth != null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        userId = auth.id,
                        role = auth.role,
                        error = null
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = response.error ?: "Registration failed"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Registration failed"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _state.value = AuthUiState()
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(
            error = null
        )
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                val authDataStore = AuthDataStore(context.applicationContext)
                val repository = AuthRepository(authDataStore)
                return AuthViewModel(repository, authDataStore) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            combine(
                authDataStore.token,
                authDataStore.role,
                authDataStore.userId
            ) { token, role, userId ->
                Triple(token, role, userId)
            }.collect { (token, role, userId) ->

                if (
                    token != null &&
                    role != null &&
                    userId != null
                ) {
                    _state.value = _state.value.copy(
                        isAuthenticated = true,
                        userId = userId,
                        role = UserRole.valueOf(role)
                    )
                } else {
                    _state.value = AuthUiState()
                }
            }
        }
    }
}
