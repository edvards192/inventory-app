package com.example.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app.viewmodel.CreateItemViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import coil.compose.AsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.navigationBarsPadding
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.layout.ContentScale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateItemScreen(
    initialEan: String = "",
    viewModel: CreateItemViewModel = viewModel(),
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var ean by remember(initialEan) {
        mutableStateOf(
            initialEan.ifBlank { state.ean }
        )
    }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->

        uri?.let {
            viewModel.addImage(uri.toString())
        }
    }
    /*LaunchedEffect(ean) {
        viewModel.onEanChange(ean)
    }*/
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Item") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF04318C),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            )
            OutlinedTextField(
                value = ean,
                onValueChange = {
                    if (it.length <= 13 && it.all(Char::isDigit)) {
                        ean = it
                    }
                },
                label = {
                    Text("EAN")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                supportingText = {
                    Text("${ean.length}/13")
                },
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Number
                    ),
                isError = ean.isNotEmpty() && ean.length != 13
            )
            Text(
                "Storage",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (state.storageLocations.isEmpty()) {
                Text("No warehouses assigned.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                state.storageLocations.forEach { storage ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${storage.warehouseCode} - ${storage.warehouseName}",
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                viewModel.decreaseStorage(
                                    storage.warehouseId
                                )
                            }
                        ) {
                            Text("-")
                        }

                        OutlinedTextField(
                            value = storage.count.toString(),

                            onValueChange = { value ->
                                if (value.isEmpty()) {
                                    viewModel.setStorageCount(
                                        storage.warehouseId,
                                        0
                                    )
                                } else {
                                    val number = value.toIntOrNull()
                                    if (number != null && number >= 0) {
                                        viewModel.setStorageCount(
                                            storage.warehouseId,
                                            number
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true,

                            keyboardOptions =
                                KeyboardOptions(
                                    keyboardType =
                                        KeyboardType.Number
                                ),

                            modifier = Modifier.width(80.dp),
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                        )
                        IconButton(
                            onClick = {
                                viewModel.increaseStorage(
                                    storage.warehouseId
                                )
                            }
                        ) {
                            Text("+")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            HorizontalDivider()

            Spacer(modifier = Modifier.height(8.dp))

            var expanded by remember {
                mutableStateOf(false)
            }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }

            ) {
                val selectedWarehouse =
                    state.availableWarehouses
                        .firstOrNull {
                            it.id == state.selectedWarehouseId
                        }

                OutlinedTextField(
                    value = selectedWarehouse?.let { "${it.code} - ${it.name}" } ?: "",
                    onValueChange = {},
                    shape = RoundedCornerShape(24.dp),
                    readOnly = true,
                    label = { Text("Select Warehouse") },
                    modifier =
                        Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                            .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    }
                ) {
                    state.availableWarehouses
                        .filter { warehouse ->
                            state.storageLocations.none {
                                it.warehouseId == warehouse.id
                            }
                        }
                        .forEach { warehouse ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${warehouse.code} - ${warehouse.name}"
                                    )
                                },
                                onClick = {
                                    viewModel.selectWarehouse(
                                        warehouse.id
                                    )
                                    expanded = false
                                }
                            )
                        }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    viewModel.addWarehouse()
                },
                enabled = state.selectedWarehouseId != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Warehouse")
            }

            OutlinedButton(
                onClick = {
                    imagePickerLauncher.launch("image/*")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Image")
            }
            if (state.imageUris.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(state.imageUris) { imageUri ->
                        Box {
                            Card {
                                AsyncImage(
                                    model = imageUri,
                                    contentDescription = null,
                                    modifier = Modifier.size(72.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .clickable {
                                        viewModel.removeImage(imageUri)
                                    },
                                color = MaterialTheme.colorScheme.error,
                                shape = RoundedCornerShape(bottomStart = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Delete image",
                                    tint = Color.White,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }
            state.error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.createItem(
                        context = context,
                        ean = ean,
                        onSuccess = {
                            Toast.makeText(
                                context,
                                "Item created successfully.",
                                Toast.LENGTH_SHORT
                            ).show()
                            onSuccess()
                        }
                    )
                },
                enabled = !state.isLoading && state.title.isNotBlank() && ean.length == 13,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (state.isLoading)
                        "Creating..."
                    else
                        "Create"
                )
            }
        }
    }
}