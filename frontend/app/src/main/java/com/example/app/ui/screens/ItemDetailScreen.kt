package com.example.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import coil.compose.AsyncImage
import com.example.app.R
import com.example.app.viewmodel.ItemDetailViewModel
import com.example.app.domain.util.getListImage
import com.example.app.domain.util.fullUrl
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import android.widget.Toast
import coil.request.ImageRequest
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: Int,
    onBack: () -> Unit,
    onDeleteSuccess: () -> Unit,
    viewModel: ItemDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.uploadImage(
                context = context,
                itemId = itemId,
                uri = it
            )
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(itemId) {
        viewModel.loadItem(itemId)
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = { Text(state.item?.title ?: "Item Details", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF04318C),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.item != null && !state.isEditing) {
                        IconButton(onClick = { viewModel.startEditing() }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.error!!)
                }
            }
            state.item != null -> {
                val item = state.item!!
                val images = remember(item.images) {
                    item.images.sortedBy { it.sortOrder }
                }
                val pagerState = rememberPagerState(pageCount = { images.size.coerceAtLeast(1) })
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ){
                    if (images.isEmpty()) {
                        AsyncImage(
                            model = item.getListImage(),
                            contentDescription = null,
                            placeholder = painterResource(R.drawable.ic_placeholder),
                            error = painterResource(R.drawable.ic_placeholder),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.75f)
                        )
                    } else {
                        Box {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.75f)
                            ) { page ->
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(images[page].fullUrl())
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    placeholder = painterResource(R.drawable.ic_placeholder),
                                    error = painterResource(R.drawable.ic_placeholder),
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            // Page indicator dots
                            if (images.size > 1) {
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    repeat(images.size) { index ->
                                        val selected = pagerState.currentPage == index
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = if (selected)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            modifier = Modifier.size(if (selected) 8.dp else 6.dp)
                                        ) {}
                                    }
                                }
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // VIEW MODE
                        if (!state.isEditing) {
                            Text(text = item.title, style = MaterialTheme.typography.headlineSmall)
                            Text("EAN: ${item.ean}")
                            Spacer(modifier = Modifier.height(16.dp))

                            item.storageLocations.forEach { storage ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${storage.warehouseCode} - ${storage.warehouseName}")
                                    Text(storage.count.toString())
                                }
                            }
                            HorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Quantity", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    item.storageLocations
                                        .sumOf { it.count }
                                        .toString(),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // EDIT MODE

                        if (state.isEditing) {
                            OutlinedTextField(
                                value = state.editTitle,
                                onValueChange = viewModel::onTitleChange,
                                label = { Text("Title") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp)
                            )
                            OutlinedTextField(
                                value = state.editEan,
                                onValueChange = viewModel::onEanChange,
                                label = { Text("EAN") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                supportingText = { Text("${state.editEan.length}/13") },
                                isError = state.editEan.isNotEmpty() && state.editEan.length != 13,
                                keyboardOptions =
                                    KeyboardOptions(
                                        keyboardType =
                                            KeyboardType.Number
                                    )
                            )
                            Text("Storage", style = MaterialTheme.typography.titleMedium)

                            Spacer(modifier = Modifier.height(8.dp))
                            if (state.editStorageLocations.isEmpty()){
                                Text("No warehouses assigned.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                            state.editStorageLocations.forEach { storage ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${storage.warehouseCode} - ${storage.warehouseName}",
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        modifier = Modifier.size(40.dp),
                                        onClick = {
                                            viewModel.decreaseStorage(
                                                storage.warehouseId
                                            )
                                        }
                                    ) {
                                        Text("-")
                                    }

                                    OutlinedTextField(
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                                        shape = RoundedCornerShape(24.dp),
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
                                        singleLine = true,
                                        modifier = Modifier.width(80.dp)
                                    )
                                    IconButton(
                                        modifier = Modifier.size(40.dp),
                                        onClick = {
                                            viewModel.increaseStorage(storage.warehouseId)
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
                                    value = selectedWarehouse?.name ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    shape = RoundedCornerShape(24.dp),
                                    label = { Text("Select Warehouse") },
                                    modifier =
                                        Modifier
                                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                                            .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    state.availableWarehouses
                                        .filter { warehouse ->
                                            state.editStorageLocations.none {
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
                                modifier = Modifier.fillMaxWidth(),
                                enabled = state.selectedWarehouseId != null
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
                            if (images.isNotEmpty()) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(items = images,
                                        key = { it.id }
                                    ) { image ->
                                        Box {
                                            Card {
                                                AsyncImage(
                                                    model = image.fullUrl(),
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(72.dp)
                                                )
                                            }
                                            Surface(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .clickable {
                                                        viewModel.deleteImage(
                                                            itemId = item.id,
                                                            imageId = image.id
                                                        )
                                                    },
                                                color = MaterialTheme.colorScheme.error,
                                                shape = RoundedCornerShape(bottomStart = 8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Delete image",
                                                    tint = Color.White,
                                                    modifier = Modifier.padding(4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.updateItem(itemId) {
                                            viewModel.cancelEditing()
                                            viewModel.loadItem(itemId)
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "Item updated successfully."
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled =
                                        state.editTitle.isNotBlank()
                                        && (state.editEan.isEmpty()
                                        || state.editEan.length == 13)

                                ) {
                                    Text("Save")
                                }
                                OutlinedButton(
                                    onClick = { viewModel.cancelEditing() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Cancel")
                                }
                            }
                            // DELETE only visible in edit mode
                            Button(
                                onClick = { showDeleteDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
                // DELETE DIALOG
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Delete Item") },
                        text = { Text("Are you sure you want to delete this item?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.deleteItem(item.id) {
                                        showDeleteDialog = false
                                        Toast.makeText(
                                            context,
                                            "Item deleted successfully.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onDeleteSuccess()
                                    }
                                }
                            ) {
                                Text("Delete")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}