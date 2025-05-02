package com.example.dormhopfrontend.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dormhopfrontend.model.RoomDto
import com.example.dormhopfrontend.viewmodel.KnockViewModel
import com.example.dormhopfrontend.viewmodel.SearchViewModel
import kotlinx.coroutines.launch

/** 1) RoomCard component */
@Composable
fun RoomCard(
    room: RoomDto,
    isSaved: Boolean,
    isKnocked: Boolean,
    onClick: (RoomDto) -> Unit,
    onSaveClick: (RoomDto) -> Unit,
    onKnockClick: (RoomDto) -> Unit
) {
    val occupancyLabel = when (room.occupancy) {
        1 -> "Single Dormitory"
        2 -> "Double Dormitory"
        3 -> "Triple Dormitory"
        4 -> "Quad Dormitory"
        else -> "${room.occupancy}-Person Dormitory"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.medium,
        elevation= CardDefaults.cardElevation(4.dp),
        colors   = CardDefaults.cardColors(containerColor = Color(0xFFF8F0EE))
    ) {
        Box {
            // 1) The main tappable area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(room) }
                    .padding(16.dp)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color.LightGray)
                )
                Spacer(Modifier.height(8.dp))
                Text(occupancyLabel, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("${room.roomNumber} ${room.dorm}", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { onKnockClick(room) },
                    enabled = !isKnocked,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isKnocked) "Knocked ✔" else "👋 Send a knock",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // 2) Heart Icon to save the dorm
            IconButton(
                onClick = { onSaveClick(room) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isSaved) "Unsave dorm" else "Save dorm",
                    tint = if (isSaved) Color.Red else LocalContentColor.current
                )
            }
        }
    }
}

/** 2) Filter sheet content */
@Composable
private fun FilterSheetContent(
    selectedOccupancies: Set<Int>,
    selectedCampuses: Set<String>,
    showRecommended: Boolean,
    onOccupancyToggle: (Int, Boolean) -> Unit,
    onCampusToggle: (String, Boolean) -> Unit,
    onRecommendedToggle: (Boolean)->Unit,
    onApply: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Switch(
            checked = showRecommended,
            onCheckedChange = onRecommendedToggle
        )
        Spacer(Modifier.width(8.dp))
        Text("Sort by Recommended")
    }

    Spacer(Modifier.height(24.dp))

    Column(Modifier.padding(16.dp)) {
        Text("Filter by occupancy", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        listOf(1, 2, 3, 4).forEach { occ ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = occ in selectedOccupancies,
                    onCheckedChange = { onOccupancyToggle(occ, it) }
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    when (occ) {
                        1 -> "Single"
                        2 -> "Double"
                        3 -> "Triple"
                        4 -> "Quad"
                        else -> "$occ-Person"
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Filter by campus", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        listOf("North", "West", "South").forEach { campus ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = campus in selectedCampuses,
                    onCheckedChange = { onCampusToggle(campus, it) }
                )
                Spacer(Modifier.width(4.dp))
                Text(campus)
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(onClick = onApply, Modifier.fillMaxWidth()) {
            Text("Apply filters")
        }
    }
}

/** 3) Main SearchScreen with bottom‐sheet for filters */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onRoomClick: (RoomDto) -> Unit
) {
    val query by viewModel.query.collectAsState()
    val rooms by viewModel.rooms.collectAsState()
    val savedIds by viewModel.savedIds.collectAsState()
    val selectedOccs by viewModel.selectedOccupancies.collectAsState()
    val selectedCampuses by viewModel.selectedCampuses.collectAsState()
    val showRec by viewModel.showRecommended.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    //implements the knock features
    val knockVm: KnockViewModel = hiltViewModel()
    val sentKnocks by knockVm.sent.collectAsState()
    val knockedIds by remember(sentKnocks) {
        derivedStateOf { sentKnocks.map { it.to_room.id }.toSet() }
    }

    LaunchedEffect(Unit) {
        knockVm.loadAll()
    }

    Scaffold { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.query.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                placeholder = { Text("Dorm feature, location…") },
                singleLine = true,
                trailingIcon = {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Filter",
                        modifier = Modifier
                            .clickable { showBottomSheet = true }
                            .padding(8.dp)
                    )
                }
            )

            Spacer(Modifier.height(4.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    bottom = 72.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rooms) { room ->
                    RoomCard(
                        room = room,
                        isSaved = room.id in savedIds,
                        isKnocked = room.id in knockedIds,
                        onClick = { onRoomClick(room) },
                        onSaveClick = { viewModel.toggleSave(room) },
                        onKnockClick = { knockVm.sendKnock(room.id) { } }
                    )
                }
                item {
                    Button(
                        onClick = { viewModel.loadMore() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Load more dorms")
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                scope.launch { sheetState.hide() }
            },
            sheetState = sheetState
        ) {
            FilterSheetContent(
                selectedOccupancies = selectedOccs,
                selectedCampuses    = selectedCampuses,
                showRecommended     = showRec,                 // ← pass through
                onOccupancyToggle   = { occ, on -> viewModel.toggleOccupancy(occ, on) },
                onCampusToggle      = { campus, on -> viewModel.toggleCampus(campus, on) },
                onRecommendedToggle = { viewModel.setShowRecommended(it) },
                onApply             = {
                    showBottomSheet = false
                    scope.launch { sheetState.hide() }
                }
            )
        }
    }
}