package com.example.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import android.content.res.Configuration
import coil.compose.AsyncImage
import com.example.app.R
import com.example.app.viewmodel.ItemListViewModel
import com.example.app.domain.util.getListImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.filled.KeyboardArrowUp
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Tune
import androidx.lifecycle.compose.collectAsStateWithLifecycle


import androidx.compose.foundation.lazy.grid.GridItemSpan
import com.example.app.ui.navigation.AppBottomNavigation
import com.example.app.ui.navigation.MainDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemListScreen(
    viewModel: ItemListViewModel,
    onCreateClick: () -> Unit,
    onScanClick: () -> Unit,
    onItemClick: (Int) -> Unit,
    onFilterClick: () -> Unit,
    onProfileClick: () -> Unit,

) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isLandscape = remember(configuration.orientation) {
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val showScrollTop by remember {
        derivedStateOf { gridState.firstVisibleItemIndex > 2 }
    }
    val shouldLoadMore by remember(
        state.items,
        state.hasMore,
        state.isLoading,
        state.isLoadingMore
    ) {
        derivedStateOf {
            val lastVisible =
                gridState.layoutInfo
                    .visibleItemsInfo
                    .lastOrNull()
                    ?.index
                    ?: 0

                    state.hasMore &&
                    !state.isLoading &&
                    !state.isLoadingMore &&
                    state.items.size >= 25 &&
                    state.items.isNotEmpty() &&

                    lastVisible >=
                    maxOf(
                        0,
                        state.items.lastIndex - 1
                    )
        }
    }
    val columnCount = remember(configuration.screenWidthDp) {
        when {
            configuration.screenWidthDp < 500 -> 2
            configuration.screenWidthDp < 900 -> 4
            else -> 5
        }
    }
    LaunchedEffect(
        shouldLoadMore
    ) {
        if (shouldLoadMore) {
            viewModel.loadMore()
        }
    }
    /*LaunchedEffect(Unit) {
        viewModel.refresh()
    }*/
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            AnimatedVisibility(
                visible = showScrollTop,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    modifier = Modifier.then(
                        if (isLandscape) Modifier.navigationBarsPadding() else Modifier
                    ),
                    shape = CircleShape,
                    onClick = {
                        coroutineScope.launch {
                            gridState.animateScrollToItem(0)
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = "Scroll to top"
                    )
                }
            }
        },
        bottomBar = {
            AppBottomNavigation(
                selectedDestination = MainDestination.INVENTORY,
                onInventoryClick = {},
                onProfileClick = onProfileClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(Color(0xFF04318C))
            )

            PullToRefreshBox(
                isRefreshing = state.isLoading && !state.isLoadingMore,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(columnCount),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item(
                        key = "top_bar",
                        span = { GridItemSpan(columnCount) }
                    ) {
                        TopAppBar(
                            title = { Text("Inventory") },
                            windowInsets = WindowInsets(0, 0, 0, 0),
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFF04318C),
                                titleContentColor = Color.White,
                                actionIconContentColor = Color.White
                            ),
                            actions = {
                                IconButton(onClick = onScanClick) {
                                    Icon(
                                        Icons.Default.QrCodeScanner,
                                        contentDescription = "Scan"
                                    )
                                }
                                IconButton(onClick = onCreateClick) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Add Item"
                                    )
                                }
                            }
                        )
                    }

                    item(
                        key = "search",
                        span = { GridItemSpan(columnCount) }
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(32.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            OutlinedTextField(
                                value = state.searchText,
                                onValueChange = { viewModel.onSearchChange(it) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(32.dp),
                                placeholder = { Text("Search items...") },
                                textStyle = LocalTextStyle.current.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                ),
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                },
                                trailingIcon = {
                                    Row {
                                        if (state.searchText.isNotEmpty()) {
                                            IconButton(onClick = { viewModel.onSearchChange("") }) {
                                                Icon(Icons.Default.Close, contentDescription = null)
                                            }
                                        }
                                        IconButton(onClick = onFilterClick) {
                                            Icon(Icons.Default.Tune, contentDescription = "Filters")
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }

                    state.error?.let { error ->
                        item(
                            key = "error",
                            span = { GridItemSpan(columnCount) }
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = error,
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    when {
                        state.items.isEmpty() && state.isLoading -> {
                            item(
                                key = "loading",
                                span = { GridItemSpan(columnCount) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        state.items.isEmpty() && state.error != null -> Unit

                        state.items.isEmpty() -> {
                            item(
                                key = "empty",
                                span = { GridItemSpan(columnCount) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        if (state.searchText.isBlank()) {
                                            "No items found\nTap + to create an item."
                                        } else {
                                            "No items match your search."
                                        }
                                    )
                                }
                            }
                        }

                        else -> {
                            items(
                                items = state.items,
                                key = { it.id }
                            ) { item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 2.dp),
                                    onClick = { onItemClick(item.id) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column {
                                        AsyncImage(
                                            model = item.getListImage(),
                                            contentDescription = null,
                                            placeholder = painterResource(R.drawable.ic_placeholder),
                                            error = painterResource(R.drawable.ic_placeholder),
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f)
                                        )
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = item.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = item.ean,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.height(1.dp))

                                            val stock = remember(item.storageLocations) {
                                                item.storageLocations.sumOf { it.count }
                                            }
                                            Surface(shape = RoundedCornerShape(50)) {
                                                Text(
                                                    text = "Qty $stock",
                                                    modifier = Modifier.padding(
                                                        horizontal = 12.dp,
                                                        vertical = 6.dp
                                                    ),
                                                    style = MaterialTheme.typography.labelLarge
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (state.isLoadingMore) {
                                item(
                                    key = "loading_more",
                                    span = { GridItemSpan(columnCount) }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
