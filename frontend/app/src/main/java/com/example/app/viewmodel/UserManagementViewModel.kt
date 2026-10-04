package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.remote.dto.CreateUserRequest
import com.example.app.data.remote.dto.UpdateUserRequest
import com.example.app.data.remote.dto.UserResponse
import com.example.app.data.repository.UserRepository
import com.example.app.domain.model.UserRole
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserManagementUiState(
    val users: List<UserResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val actionError: String? = null,
    val isEditorOpen: Boolean = false,
    val editingUser: UserResponse? = null,
    val deletingUser: UserResponse? = null
)

class UserManagementViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {
    private val _state = MutableStateFlow(UserManagementUiState())
    val state = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        if (_state.value.isLoading || _state.value.isSaving) return
        viewModelScope.launch { loadUsers() }
    }

    private suspend fun loadUsers() {
        _state.update { it.copy(isLoading = true, error = null) }
        try {
            val users = repository.getUsers()
            _state.update { it.copy(users = users, isLoading = false) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.update { it.copy(isLoading = false, error = e.message ?: "Unable to load users") }
        }
    }

    fun edit(user: UserResponse? = null) {
        if (_state.value.isSaving) return
        _state.update { it.copy(isEditorOpen = true, editingUser = user, actionError = null) }
    }

    fun confirmDelete(user: UserResponse) {
        if (_state.value.isSaving) return
        _state.update { it.copy(deletingUser = user, actionError = null) }
    }

    fun dismissDialog() {
        if (_state.value.isSaving) return
        _state.update { it.copy(isEditorOpen = false, editingUser = null, deletingUser = null, actionError = null) }
    }

    fun save(
        name: String,
        surname: String,
        email: String,
        password: String,
        role: UserRole,
        active: Boolean,
        onUpdated: (UserResponse) -> Unit
    ) {
        val user = _state.value.editingUser
        mutate {
            if (user == null) {
                repository.createUser(CreateUserRequest(name.trim(), surname.trim(), email.trim(), password, role))
            } else {
                repository.updateUser(user.id, UpdateUserRequest(name.trim(), surname.trim(), email.trim(), role, active))
                onUpdated(user.copy(name = name.trim(), surname = surname.trim(), email = email.trim(), role = role, isActive = active))
            }
        }
    }

    fun setActive(user: UserResponse) = mutate { repository.setActive(user.id, !user.isActive) }

    fun delete() {
        val user = _state.value.deletingUser ?: return
        mutate { repository.deleteUser(user.id) }
    }

    private fun mutate(action: suspend () -> Unit) {
        if (_state.value.isSaving || _state.value.isLoading) return
        _state.update { it.copy(isSaving = true, actionError = null) }
        viewModelScope.launch {
            try {
                action()
                _state.update {
                    it.copy(isEditorOpen = false, editingUser = null, deletingUser = null)
                }
                loadUsers()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(actionError = e.message ?: "Unable to save changes") }
            } finally {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }
}
