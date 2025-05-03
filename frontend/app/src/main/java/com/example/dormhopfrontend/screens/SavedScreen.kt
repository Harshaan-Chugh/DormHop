package com.example.dormhopfrontend.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dormhopfrontend.viewmodel.SavedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    viewModel: SavedViewModel = hiltViewModel(),
    onRoomClick: (Int) -> Unit = {}
) {
    val rooms   by viewModel.rooms.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error   by viewModel.error.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            // Center‐aligned title, fixed 56.dp height so the bar is tight
            CenterAlignedTopAppBar(
                modifier = Modifier.height(56.dp),
                title = {
                    Text(
                        "Saved Dorms",
                        modifier  = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                error != null -> {
                    Text(
                        text     = error!!,
                        color    = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                rooms.isEmpty() -> {
                    // Empty state with heart icon, nudged up under the AppBar
                    Column(
                        modifier           = Modifier
                            .align(Alignment.Center)
                            .offset(y = (-20).dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            modifier           = Modifier.size(72.dp),
                            tint               = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("No saved dorms",
                            style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tap the ❤️ on any dorm to save it here for later.",
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.fillMaxWidth(0.8f)
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier            = Modifier.fillMaxSize(),
                        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(rooms) { room ->
                            Card(
                                modifier  = Modifier
                                    .fillMaxWidth()
                                    .clickable { onRoomClick(room.id) },
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        text  = when (room.occupancy) {
                                            1    -> "Single dormitory"
                                            2    -> "Double dormitory"
                                            3    -> "Triple dormitory"
                                            else -> "${room.occupancy}-person dorm"
                                        },
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text  = "${room.roomNumber} ${room.dorm}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = { onRoomClick(room.id) },
                                            modifier = Modifier.weight(1f)) {
                                            Text("More Information")
                                        }
                                        IconButton(onClick = { viewModel.unsave(room.id) }) {
                                            Icon(Icons.Default.Close, contentDescription = "Unsave")
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
