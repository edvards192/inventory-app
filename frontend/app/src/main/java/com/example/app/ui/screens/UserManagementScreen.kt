package com.example.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.data.remote.dto.UserResponse
import com.example.app.domain.model.UserRole
import com.example.app.viewmodel.UserManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    currentUserId: Int,
    onBack: () -> Unit,
    onUserUpdated: (UserResponse) -> Unit,
    viewModel: UserManagementViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val busy = state.isLoading || state.isSaving

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User management") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh, enabled = !busy) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh users")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF04318C),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (!busy) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.edit() },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Create user") }
                )
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            state.error?.let { error ->
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(error, Modifier.weight(1f), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = viewModel::refresh, enabled = !busy) { Text("Retry") }
                }
            }
            if (!state.isEditorOpen && state.deletingUser == null) {
                state.actionError?.let {
                    Text(it, Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
                }
            }
            if (state.users.isEmpty() && !state.isLoading && state.error == null) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No users yet. Create a user to get started.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.users, key = { it.id }) { user ->
                        val isSelf = user.id == currentUserId
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "${user.name} ${user.surname}" + if (isSelf) " (you)" else "",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(user.email, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${user.role.name} · ${if (user.isActive) "Active" else "Inactive"}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    TextButton(onClick = { viewModel.edit(user) }, enabled = !busy) { Text("Edit") }
                                    TextButton(
                                        onClick = { viewModel.setActive(user) },
                                        enabled = !busy && !isSelf
                                    ) { Text(if (user.isActive) "Deactivate" else "Activate") }
                                    TextButton(
                                        onClick = { viewModel.confirmDelete(user) },
                                        enabled = !busy && !isSelf,
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) { Text("Delete") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.isEditorOpen) {
        UserEditorDialog(
            user = state.editingUser,
            isSelf = state.editingUser?.id == currentUserId,
            busy = busy,
            error = state.actionError,
            onDismiss = viewModel::dismissDialog,
            onSave = { name, surname, email, password, role, active ->
                viewModel.save(name, surname, email, password, role, active, onUserUpdated)
            }
        )
    }

    state.deletingUser?.let { user ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDialog,
            title = { Text("Delete user?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Permanently delete ${user.name} ${user.surname} (${user.email})? This cannot be undone.")
                    state.actionError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::delete, enabled = !busy) {
                    Text(if (state.isSaving) "Deleting…" else "Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDialog, enabled = !busy) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun UserEditorDialog(
    user: UserResponse?,
    isSelf: Boolean,
    busy: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, UserRole, Boolean) -> Unit
) {
    var name by rememberSaveable(user?.id) { mutableStateOf(user?.name.orEmpty()) }
    var surname by rememberSaveable(user?.id) { mutableStateOf(user?.surname.orEmpty()) }
    var email by rememberSaveable(user?.id) { mutableStateOf(user?.email.orEmpty()) }
    var password by remember { mutableStateOf("") }
    var role by rememberSaveable(user?.id) { mutableStateOf(user?.role ?: UserRole.VIEW) }
    var active by rememberSaveable(user?.id) { mutableStateOf(user?.isActive ?: true) }
    var submitted by rememberSaveable(user?.id) { mutableStateOf(false) }
    val nameInvalid = name.trim().isEmpty() || name.trim().length > 100
    val surnameInvalid = surname.trim().isEmpty() || surname.trim().length > 100
    val emailInvalid = email.trim().length > 255 || !Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email.trim())
    val passwordInvalid = user == null && (password.isBlank() || password.toByteArray(Charsets.UTF_8).size > 72)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (user == null) "Create user" else "Edit user") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it }, label = { Text("Name") },
                    singleLine = true, enabled = !busy, isError = submitted && nameInvalid,
                    supportingText = if (submitted && nameInvalid) { { Text("Enter 1–100 characters") } } else null
                )
                OutlinedTextField(
                    value = surname, onValueChange = { surname = it }, label = { Text("Surname") },
                    singleLine = true, enabled = !busy, isError = submitted && surnameInvalid,
                    supportingText = if (submitted && surnameInvalid) { { Text("Enter 1–100 characters") } } else null
                )
                OutlinedTextField(
                    value = email, onValueChange = { email = it }, label = { Text("Email") },
                    singleLine = true, enabled = !busy, isError = submitted && emailInvalid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    supportingText = if (submitted && emailInvalid) { { Text("Enter a valid email address") } } else null
                )
                if (user == null) {
                    OutlinedTextField(
                        value = password, onValueChange = { password = it }, label = { Text("Password") },
                        singleLine = true, enabled = !busy, isError = submitted && passwordInvalid,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        supportingText = if (submitted && passwordInvalid) {
                            { Text("Required; maximum 72 UTF-8 bytes") }
                        } else null
                    )
                }
                Text("Role", style = MaterialTheme.typography.labelLarge)
                UserRole.entries.forEach { option ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = role == option,
                            onClick = { role = option },
                            enabled = !busy && !isSelf
                        )
                        Text(option.name)
                    }
                }
                if (user != null) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Active", Modifier.weight(1f))
                        Switch(checked = active, onCheckedChange = { active = it }, enabled = !busy && !isSelf)
                    }
                } else {
                    Text("New users are active and can log in immediately.")
                }
                if (isSelf) Text("You cannot deactivate your own account or remove your admin role.")
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !busy,
                onClick = {
                    submitted = true
                    if (!nameInvalid && !surnameInvalid && !emailInvalid && !passwordInvalid) {
                        onSave(name, surname, email, password, role, active)
                    }
                }
            ) { Text(if (busy) "Saving…" else "Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancel") }
        }
    )
}
