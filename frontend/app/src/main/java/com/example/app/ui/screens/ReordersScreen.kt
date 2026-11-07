package com.example.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app.domain.model.Reorder
import com.example.app.domain.model.ReorderStatus
import com.example.app.ui.navigation.AppBottomNavigation
import com.example.app.ui.navigation.MainDestination
import com.example.app.viewmodel.ReordersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReordersScreen(
    viewModel: ReordersViewModel,
    onInventoryClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        viewModel.startPolling()
        onDispose {
            viewModel.stopPolling()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AppBottomNavigation(
                selectedDestination = MainDestination.ORDERS,
                onInventoryClick = onInventoryClick,
                onOrdersClick = {},
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
                isRefreshing = state.isLoading,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item(key = "top_bar") {
                        TopAppBar(
                            title = { Text("Reorders") },
                            windowInsets = WindowInsets(0, 0, 0, 0),
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFF04318C),
                                titleContentColor = Color.White
                            )
                        )
                    }

                    state.error?.let { error ->
                        item(key = "error") {
                            Text(
                                text = error,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    if (state.isLoading && state.queue.isEmpty() && state.history.isEmpty()) {
                        item(key = "loading") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        return@LazyColumn
                    }

                    item(key = "queue_header") {
                        SectionHeader(
                            text = "Processing Queue",
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    if (state.queue.isEmpty()) {
                        item(key = "queue_empty") {
                            EmptyText("No reorders in queue")
                        }
                    } else {
                        items(state.queue, key = { "queue_${it.id}" }) { reorder ->
                            QueueRow(reorder)
                        }
                    }

                    item(key = "history_header") {
                        SectionHeader(
                            text = "History",
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
                        )
                    }

                    if (state.history.isEmpty()) {
                        item(key = "history_empty") {
                            EmptyText("No completed reorders yet")
                        }
                    } else {
                        items(state.history, key = { "history_${it.id}" }) { reorder ->
                            HistoryRow(reorder)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun EmptyText(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = 16.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun QueueRow(reorder: Reorder) {
    ReorderCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "#${reorder.id}",
                fontWeight = FontWeight.Bold
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reorder.itemTitle,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Quantity: ${reorder.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusBadge(reorder.status)
        }
    }
}

@Composable
private fun HistoryRow(reorder: Reorder) {
    ReorderCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "#${reorder.id}",
                fontWeight = FontWeight.Bold
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${reorder.itemTitle} × ${reorder.quantity}",
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Created: ${formatTimestamp(reorder.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Completed: ${reorder.completedAt?.let { formatTimestamp(it) } ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusBadge(reorder.status)
        }
    }
}

@Composable
private fun ReorderCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            content()
        }
    }
}

@Composable
private fun StatusBadge(status: ReorderStatus) {
    val color = when (status) {
        ReorderStatus.WAITING -> Color(0xFF757575)
        ReorderStatus.PROCESSING -> Color(0xFFEF6C00)
        ReorderStatus.COMPLETED -> Color(0xFF2E7D32)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

private fun formatTimestamp(value: String): String {
    return value.replace("T", " ").take(16)
}
