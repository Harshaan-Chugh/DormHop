package com.example.dormhopfrontend.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.dormhopfrontend.model.RoomDto
import com.example.dormhopfrontend.viewmodel.DetailViewModel
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    roomId: Int,
    viewModel: DetailViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
) {
    val roomState = viewModel.room.collectAsState(initial = null)
    val room = roomState.value

    val features by viewModel.features
        .collectAsState(initial = emptyList())

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
            } else{
                RoomDetailsContent(room = room!!, features = features)
            }
        }
    }
}


//basically shows all teh content of the room
@Composable
fun RoomDetailsContent(
    room: RoomDto,
    features: List<String> = emptyList(),
    ownerName : String? = null,
    ownerClass: Int?    = null
) {
    val occupancyLabel = when (room.occupancy) {
        1 -> "Single Dormitory"
        2 -> "Double Dormitory"
        3 -> "Triple Dormitory"
        4 -> "Quad Dormitory"
        else -> "${room.occupancy}-Person Dormitory"
    }
    val roomTitle = "${room.dorm} ${room.roomNumber}"

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        /* placeholder image / map */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.LightGray)
        )

        Spacer(Modifier.height(16.dp))

        /* main fields */
        Text(occupancyLabel, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(roomTitle,      style = MaterialTheme.typography.bodyMedium)

        /* owner line – only if supplied */
        if (ownerName != null && ownerClass != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Posted by $ownerName · Class of $ownerClass",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        /* amenities */
        if (room.amenities.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("Amenities:", style = MaterialTheme.typography.titleSmall)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                room.amenities.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
            }
        }

        /* description */
        room.description?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(8.dp))
            Text("Description:", style = MaterialTheme.typography.titleSmall)
            Text(it, style = MaterialTheme.typography.bodyMedium)
        }

        /* community features */
        if (features.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text("Community features:", style = MaterialTheme.typography.titleSmall)

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(start = 8.dp)   // small indent
            ) {
                features.forEach { feat ->
                    Text("- $feat", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}