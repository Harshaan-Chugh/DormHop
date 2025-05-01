package com.example.dormhopfrontend.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dormhopfrontend.viewmodel.DetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    roomId: Int,
    onBack: () -> Unit = {},
    viewModel: DetailViewModel = hiltViewModel()
) {
    val roomState = viewModel.room.collectAsState(initial = null)
    val room = roomState.value

    // Trigger load on first composition or roomId change
    LaunchedEffect(roomId) {
        Log.d("DetailScreen", "Loading room $roomId")
        viewModel.load(roomId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(room?.dorm ?: "Loading…") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (room == null) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Placeholder map/image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color.LightGray)
                    )
                    Spacer(Modifier.height(16.dp))

                    // Occupancy label
                    val occupancyLabel = when (room.occupancy) {
                        1 -> "Single Dormitory"
                        2 -> "Double Dormitory"
                        3 -> "Triple Dormitory"
                        else -> "${room.occupancy}-Person Dormitory"
                    }
                    Text(
                        text = occupancyLabel,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))

                    // Room and Dorm info
                    Text(
                        text = "${room.dorm} #${room.roomNumber}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))

                    Spacer(Modifier.height(16.dp))
                    // TODO: add "Send Knock" or other actions
                }
            }
        }
    }
}
