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
import com.example.dormhopfrontend.viewmodel.SearchViewModel

/** Represents a single dorm listing */
data class DormItem(val title: String, val address: String)

/** Some dummy listings to render for now */
private val sampleDorms = listOf(
    DormItem("Single Dormitory", "488 McFaddin Hall, West Campus"),
    DormItem("Double Dormitory", "422 McFaddin Hall, West Campus"),
    DormItem("Triple Dormitory", "563 Carl Becker House, North Campus")
)

@Composable
fun DormCard(dorm: DormItem, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.LightGray)
            )
            Spacer(Modifier.height(8.dp))

            Text(dorm.title, style = MaterialTheme.typography.titleMedium)
            Text(dorm.address, style = MaterialTheme.typography.bodyMedium)

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
    onDormClick: (DormItem) -> Unit
) {
    val filterQuery by viewModel.query.collectAsState()
    val rooms by viewModel.rooms.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // 1) Filter bar at top
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

        // 2) Scrollable list of dorm cards
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                bottom = 56.dp,    // leave space for bottom nav
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(rooms.map { roomDto ->
                DormItem(title = roomDto.dorm, address = roomDto.roomNumber)
            }) { dormItem ->
                DormCard(dormItem) {
                    onDormClick(dormItem)
                }
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

