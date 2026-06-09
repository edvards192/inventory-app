package com.example.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.res.Configuration
import coil.compose.AsyncImage
import com.example.app.R
import com.example.app.viewmodel.ItemListViewModel
import com.example.app.domain.util.getListImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemListScreen(
    viewModel: ItemListViewModel = viewModel(),
    message: String = "",
    onCreateClick: () -> Unit,
    onScanClick: () -> Unit,
    onItemClick: (Int) -> Unit

) {
    val state by viewModel.state.collectAsState()
    val configuration = LocalConfiguration.current
    val snackbarHostState = remember { SnackbarHostState() }
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val columnCount = when {
        configuration.screenWidthDp < 500 -> 2
        configuration.screenWidthDp < 900 -> 3
        else -> 4
    }

    val scrollBehavior = if (isLandscape) {
        TopAppBarDefaults.enterAlwaysScrollBehavior()
    } else {
        TopAppBarDefaults.pinnedScrollBehavior()
    }
    LaunchedEffect(message) {

        when(message) {

            "create" ->
                snackbarHostState.showSnackbar(
                    "Item created successfully."
                )

            "update" ->
                snackbarHostState.showSnackbar(
                    "Item updated successfully."
                )

            "delete" ->
                snackbarHostState.showSnackbar(
                    "Item deleted successfully."
                )
        }
    }


    Scaffold(
        snackbarHost = {
            SnackbarHost(
                snackbarHostState
            )
        },
        modifier = if (isLandscape) {
            Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        } else {
            Modifier
        },
        topBar = {
            TopAppBar(
                title = { Text("Inventory") },
                actions = {
                    ElevatedButton(
                        onClick = onScanClick
                    ) {
                        Text("Scan")
                    }

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )
                    ElevatedButton(onClick = onCreateClick) {
                        Text("Add")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            OutlinedTextField(
                value = state.searchText,
                onValueChange = {
                    viewModel.onSearchChange(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                singleLine = true,
                placeholder = {
                    Text("Search items...")
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (state.searchText.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                viewModel.onSearchChange("")
                            }
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear"
                            )
                        }
                    }
                }
            )

            state.error?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(it)
                }
            }

            if (
                state.items.isEmpty() &&
                !state.isLoading
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        if (state.searchText.isBlank())
                            "No items yet."
                        else
                            "No results found."
                    )
                }

            } else {

                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnCount),

                    modifier = Modifier.fillMaxSize(),

                    contentPadding = PaddingValues(8.dp),

                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp),

                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    items(state.items) { item ->

                        Card(
                            modifier = Modifier.fillMaxWidth(),

                            onClick = {
                                onItemClick(item.id)
                            }
                        ) {

                            Column {

                                AsyncImage(
                                    model =
                                        item.getListImage()
                                            ?: "",

                                    contentDescription = null,

                                    placeholder =
                                        painterResource(
                                            R.drawable.ic_placeholder
                                        ),

                                    error =
                                        painterResource(
                                            R.drawable.ic_placeholder
                                        ),

                                    contentScale =
                                        ContentScale.Crop,

                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(120.dp)
                                )

                                Column(
                                    modifier =
                                        Modifier.padding(8.dp)
                                ) {

                                    Text(
                                        item.title,
                                        style =
                                            MaterialTheme
                                                .typography
                                                .titleSmall
                                    )

                                    Text(
                                        "EAN: ${item.ean}",
                                        style =
                                            MaterialTheme
                                                .typography
                                                .bodySmall
                                    )

                                    Text(
                                        "Images: ${item.images.size}"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}