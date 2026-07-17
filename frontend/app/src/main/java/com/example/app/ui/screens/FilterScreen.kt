package com.example.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app.viewmodel.ItemListViewModel
import androidx.compose.ui.Alignment
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.clickable
import com.example.app.domain.model.StockFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    viewModel: ItemListViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var selectedSort by remember(state.sort) { mutableStateOf(state.sort) }
    var selectedStockFilter by remember(state.stockFilter) { mutableStateOf(state.stockFilter) }
    var selectedWarehouses by remember(state.warehouseIds) { mutableStateOf(state.warehouseIds) }
    var minQuantityText by remember(state.minQuantity) { mutableStateOf(state.minQuantity?.toString() ?: "") }
    var maxQuantityText by remember(state.maxQuantity) { mutableStateOf(state.maxQuantity?.toString() ?: "") }

    val min = minQuantityText.toIntOrNull()
    val max = maxQuantityText.toIntOrNull()
    val quantityValid = min == null || max == null || min <= max

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filter & Sort") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF04318C),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.applyFilters(
                            sort = "updatedDesc",
                            stockFilter = StockFilter.ALL,
                            warehouseIds = emptyList(),
                            minQuantity = null,
                            maxQuantity = null
                        )
                        onBack()
                    }
                ) { Text("Clear") }

                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedSort = "updatedDesc"
                        selectedStockFilter = StockFilter.ALL
                        selectedWarehouses = emptyList()
                        minQuantityText = ""
                        maxQuantityText = ""
                        viewModel.applyFilters(
                            sort = "updatedDesc",
                            stockFilter = StockFilter.ALL,
                            warehouseIds = emptyList(),
                            minQuantity = null,
                            maxQuantity = null
                        )
                    }
                ) { Text("Reset") }

                Button(
                    modifier = Modifier.weight(1f),
                    enabled = quantityValid,
                    onClick = {
                        viewModel.applyFilters(
                            sort = selectedSort,
                            stockFilter = selectedStockFilter,
                            warehouseIds = selectedWarehouses,
                            minQuantity = minQuantityText.toIntOrNull(),
                            maxQuantity = maxQuantityText.toIntOrNull()
                        )
                        onBack()
                    }
                ) { Text("Apply") }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .padding(bottom = padding.calculateBottomPadding())
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            SectionHeader("Sort by")
            Spacer(Modifier.height(4.dp))

            listOf(
                "updatedDesc" to "Recently updated",
                "nameAsc" to "Name A–Z",
                "nameDesc" to "Name Z–A"
            ).forEach { (value, label) ->
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedSort = value }) {
                    RadioButton(
                        selected = selectedSort == value,
                        onClick  = null,
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                }
            }
            SectionDivider()
            SectionHeader("Stock")
            listOf(
                StockFilter.ALL to "All",
                StockFilter.IN_STOCK to "In stock",
                StockFilter.OUT_OF_STOCK to "Out of stock"
            ).forEach {
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedStockFilter = it.first }
                ) {
                    RadioButton(
                        selected = selectedStockFilter == it.first,
                        onClick  = null,
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(it.second, style = MaterialTheme.typography.bodyMedium)
                }
            }
            SectionDivider()

            SectionHeader("Quantity")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = minQuantityText,
                    onValueChange = { if (it.isEmpty() || it.all(Char::isDigit)) minQuantityText = it },
                    label = { Text("Min") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(24.dp),
                )
                OutlinedTextField(
                    value = maxQuantityText,
                    onValueChange = { if (it.isEmpty() || it.all(Char::isDigit)) maxQuantityText = it },
                    label = { Text("Max") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(24.dp),
                )
            }
            SectionDivider()

            SectionHeader("Warehouses")
            Spacer(Modifier.height(4.dp))
            state.availableWarehouses
                .sortedBy { it.name }
                .forEach { warehouse ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val checked = warehouse.id !in selectedWarehouses
                                selectedWarehouses =
                                    if (checked)
                                        selectedWarehouses + warehouse.id
                                    else
                                        selectedWarehouses.filterNot { it == warehouse.id }
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = warehouse.id in selectedWarehouses,
                            onCheckedChange = { checked ->
                                selectedWarehouses =
                                    if (checked)
                                        selectedWarehouses + warehouse.id
                                    else
                                        selectedWarehouses.filterNot { it == warehouse.id }
                            }
                        )
                        Column {
                            Text(warehouse.code, style = MaterialTheme.typography.labelLarge)
                            Text(warehouse.name, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            Spacer(Modifier.height(16.dp))
        }
    }
}
@Composable
private fun SectionHeader(title: String) {
    Text(
        text  = title,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF04318C),
            letterSpacing = androidx.compose.ui.unit.TextUnit(
                1.1f, androidx.compose.ui.unit.TextUnitType.Sp
            )
        )
    )
}

@Composable
private fun SectionDivider() {
    Spacer(Modifier.height(12.dp))
    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    Spacer(Modifier.height(16.dp))
}