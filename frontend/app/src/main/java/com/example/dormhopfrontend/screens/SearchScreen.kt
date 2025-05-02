package com.example.dormhopfrontend.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dormhopfrontend.model.OwnerDto
import com.example.dormhopfrontend.model.RoomDto
import com.example.dormhopfrontend.viewmodel.SearchViewModel

/** Represents a single dorm listing */
data class DormItem(val title: String, val address: String)

@Composable
fun RoomCard(
    room: RoomDto,
    onClick: (RoomDto) -> Unit
) {
    // Map occupancy number to human-friendly string
    val occupancyLabel = when (room.occupancy) {
        1 -> "Single Dormitory"
        2 -> "Double Dormitory"
        3 -> "Triple Dormitory"
        4 -> "Quad Dormitory"
        else -> "${room.occupancy}-Person Dormitory"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(room) }
            .background(Color(0x22A34635)),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.LightGray)
            )
            Spacer(Modifier.height(8.dp))

            Text(occupancyLabel, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${room.roomNumber} ${room.dorm}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { /* TODO: send a knock */ },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("👋 Send a knock", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}


@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onRoomClick: (RoomDto) -> Unit
) {
    val filterQuery by viewModel.query.collectAsState()
    val filteredRooms by viewModel.rooms.collectAsState()

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = filterQuery,
            onValueChange = { viewModel.query.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            placeholder = { Text("Dorm feature, location…") },
            singleLine = true,
            trailingIcon = {
                Icon(Icons.Default.FilterList, contentDescription = "Filter")
            }
        )

        Spacer(Modifier.height(4.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                bottom = 56.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredRooms) { room ->
                RoomCard(room) { onRoomClick(it) }
            }

            item {
                Button(
                    onClick = { /* TODO: load more dorms */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Load more dorms")
                }
            }
        }
    }
}



