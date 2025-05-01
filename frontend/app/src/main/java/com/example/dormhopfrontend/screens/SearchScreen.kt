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

// Sample placeholders for UI consistency
private val sampleRooms = listOf(
    RoomDto(
        id = -1,
        dorm = "McFaddin Hall, West Campus",
        roomNumber = "488",
        occupancy = 1,
        amenities = listOf("Air Conditioning", "High-Speed WiFi"),
        description = "Cozy single with a west-facing view",
        createdAt = "04/01/2025",
        updatedAt = "04/30/2025",
        isRoomListed = true,
        owner = OwnerDto(
            id = 201,
            email = "john.doe@cornell.edu",
            fullName = "John Doe",
            classYear = 2027
        )
    ),
    RoomDto(
        id = -2,
        dorm = "McFaddin Hall, West Campus",
        roomNumber = "422",
        occupancy = 2,
        amenities = listOf("Shared Bathroom", "Heating"),
        description = null,
        createdAt = "03/15/2025",
        updatedAt = "04/15/2025",
        isRoomListed = false,
        owner = OwnerDto(
            id = 202,
            email = "jane.smith@cornell.edu",
            fullName = "Jane Smith",
            classYear = 2026
        )
    ),
    RoomDto(
        id = -3,
        dorm = "Carl Becker House, North Campus",
        roomNumber = "563",
        occupancy = 3,
        amenities = listOf("Mini Kitchenette", "In-unit Laundry"),
        description = "Spacious triple with kitchenette",
        createdAt = "02/10/2025",
        updatedAt = "04/10/2025",
        isRoomListed = true,
        owner = OwnerDto(
            id = 203,
            email = "alex.lee@cornell.edu",
            fullName = "Alex Lee",
            classYear = 2025
        )
    )
)

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
            .clickable { onClick(room) },
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
    val dynamicRooms by viewModel.rooms.collectAsState()

    // Combine sample data and real data
    val allRooms = remember(filterQuery, dynamicRooms) {
        sampleRooms + dynamicRooms
    }

    // Apply filter to the combined list
    val filteredRooms = remember(filterQuery, allRooms) {
        if (filterQuery.isBlank()) allRooms
        else allRooms.filter { room ->
            room.dorm.contains(filterQuery, ignoreCase = true)
                    || room.roomNumber.contains(filterQuery, ignoreCase = true)
                    || room.amenities.any { it.contains(filterQuery, ignoreCase = true) }
        }
    }

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



