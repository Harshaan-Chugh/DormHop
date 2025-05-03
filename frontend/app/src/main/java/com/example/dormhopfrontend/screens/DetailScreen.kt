// app/src/main/java/com/example/dormhopfrontend/screens/DetailScreen.kt
package com.example.dormhopfrontend.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dormhopfrontend.R
import com.example.dormhopfrontend.model.RoomDto
import com.example.dormhopfrontend.viewmodel.DetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    roomId: Int,
    viewModel: DetailViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val roomState = viewModel.room.collectAsState(initial = null)
    val room      = roomState.value

    val features  by viewModel.features.collectAsState(initial = emptyList())

    // load (or reload) whenever roomId changes
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
                // still fetching…
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                // once loaded, show the shared details content
                RoomDetailsContent(
                    room = room,
                    features = features
                )
            }
        }
    }
}

/**
 * Renders an image + all of the room’s details, plus an
 * optional “posted by” line if you pass ownerName/ownerClass.
 */
@Composable
fun RoomDetailsContent(
    room: RoomDto,
    features: List<String> = emptyList(),
    ownerName: String? = null,
    ownerClass: Int?  = null
) {
    val ctx: Context = LocalContext.current

    // build a drawable‐resource name from the dorm’s title:
    // e.g. "Carl Becker House" → "carl_becker_house"
    val slug = room.dorm
        .lowercase()
        .replace(Regex("[^a-z0-9]"), "_")
        .replace(Regex("_+"), "_")
        .trim('_')

    // look up that resource, fall back to placeholder if missing
    val imageResId = ctx.resources
        .getIdentifier(slug, "drawable", ctx.packageName)
        .takeIf { it != 0 }
        ?: R.drawable.dorm_placeholder

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 1) the dorm photo
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = "${room.dorm} photo",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(16.dp))

        // 2) occupancy label & room number
        val occupancyLabel = when (room.occupancy) {
            1 -> "Single Dormitory"
            2 -> "Double Dormitory"
            3 -> "Triple Dormitory"
            4 -> "Quad Dormitory"
            else -> "${room.occupancy}-Person Dormitory"
        }
        Text(occupancyLabel, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text("${room.dorm} ${room.roomNumber}", style = MaterialTheme.typography.bodyMedium)

        // 3) “Posted by …” if available
        if (ownerName != null && ownerClass != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Posted by $ownerName · Class of $ownerClass",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // 4) amenities
        if (room.amenities.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("Amenities:", style = MaterialTheme.typography.titleSmall)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                room.amenities.forEach {
                    Text("• $it", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // 5) description
        room.description
            ?.takeIf { it.isNotBlank() }
            ?.let {
                Spacer(Modifier.height(8.dp))
                Text("Description:", style = MaterialTheme.typography.titleSmall)
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }

        if (features.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text("Community features:", style = MaterialTheme.typography.titleSmall)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(start = 8.dp)) {
                features.forEach { feat ->
                    Text("• $feat", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
