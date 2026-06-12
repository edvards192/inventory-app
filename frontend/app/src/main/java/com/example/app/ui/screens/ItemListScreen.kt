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
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemListScreen(
    viewModel: ItemListViewModel = viewModel(),
    onCreateClick: () -> Unit,
    onScanClick: () -> Unit,
    onItemClick: (Int) -> Unit

) {
    val state by viewModel.state.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val showScrollTop = gridState.firstVisibleItemIndex > 2

    val columnCount = when {
        configuration.screenWidthDp < 500 -> 2
        configuration.screenWidthDp < 900 -> 4
        else -> 5
    }
    val scrollBehavior = if (isLandscape) {
        TopAppBarDefaults.enterAlwaysScrollBehavior()
    } else {
        TopAppBarDefaults.pinnedScrollBehavior()
    }
    Scaffold(

        floatingActionButton = {

            AnimatedVisibility(
                visible = showScrollTop,
                enter = fadeIn(),
                exit = fadeOut()
            ) {

                FloatingActionButton(
                    modifier = Modifier
                        .then(
                            if (isLandscape)
                                Modifier.navigationBarsPadding()
                            else
                                Modifier
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
        modifier = if (isLandscape) {
            Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        } else {
            Modifier
        },
        topBar = {
            TopAppBar(
                title = { Text("Inventory") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF04318C),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {

                    IconButton(
                        onClick = onScanClick
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "Scan"
                        )
                    }

                    IconButton(
                        onClick = onCreateClick
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Item"
                        )
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

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                OutlinedTextField(
                    value = state.searchText,

                    onValueChange = {
                        viewModel.onSearchChange(it)
                    },

                    modifier = Modifier.fillMaxWidth(),

                    singleLine = true,

                    shape = RoundedCornerShape(32.dp),

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
                    },

                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor =
                            MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor =
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }


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
            if (state.items.isEmpty() && !state.isLoading) {
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
                PullToRefreshBox(

                    isRefreshing = state.isLoading,

                    onRefresh = {
                        viewModel.refresh()
                    }

                ){


                    LazyVerticalGrid(

                        state = gridState,

                        columns = GridCells.Fixed(columnCount),

                        modifier = Modifier.fillMaxSize(),

                        contentPadding = PaddingValues(4.dp),

                        horizontalArrangement =
                            Arrangement.spacedBy(4.dp),

                        verticalArrangement =
                            Arrangement.spacedBy(4.dp)

                    ) {
                        items(state.items) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onItemClick(item.id) },

                                //shape = RoundedCornerShape(20.dp),

                                colors = CardDefaults.cardColors(
                                    containerColor =
                                        MaterialTheme.colorScheme.surfaceVariant
                                ),


                                ) {

                                Column {

                                    AsyncImage(
                                        model = item.getListImage() ?: "",
                                        contentDescription = null,
                                        placeholder =
                                            painterResource(R.drawable.ic_placeholder),
                                        error =
                                            painterResource(R.drawable.ic_placeholder),
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                    )
                                    /* Box(
                                         Modifier
                                             .fillMaxWidth()
                                             .aspectRatio(1f)
                                     )*/
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {

                                        Text(
                                            text = item.title,

                                            style =
                                                MaterialTheme.typography.titleMedium,

                                            maxLines = 1
                                        )

                                        Text(
                                            text = item.ean,

                                            style =
                                                MaterialTheme.typography.bodySmall,

                                            color =
                                                MaterialTheme.colorScheme.onSurfaceVariant,

                                            maxLines = 1
                                        )

                                        Spacer(
                                            modifier = Modifier.height(1.dp)
                                        )

                                        val stock = remember(item.storageLocations) {
                                            item.storageLocations.sumOf { it.count }
                                        }

                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Text(
                                                text = stock.toString(),
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

                    }
                }
            }
        }
    }
}