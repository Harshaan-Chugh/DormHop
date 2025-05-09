package com.example.dormhopfrontend.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.dormhopfrontend.model.KnockResponse
import com.example.dormhopfrontend.viewmodel.KnockViewModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun KnockList(
    knocks: List<KnockResponse>,
    onItemClick: (Int) -> Unit,
    onCancel: (Int) -> Unit,
    onAccept: ((Int) -> Unit)? = null,
    onReject: ((Int) -> Unit)? = null
) {
    val dtFormatter = remember {
        DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(knocks) { knock ->

            // format created_at
            val createdAtFormatted = remember(knock.created_at) {
                runCatching {
                    OffsetDateTime.parse(knock.created_at)
                        .format(dtFormatter)
                }.getOrNull() ?: knock.created_at
            }
            val acceptedAtFormatted = knock.accepted_at?.let { raw ->
                runCatching {
                    OffsetDateTime.parse(raw)
                        .format(dtFormatter)
                }.getOrNull()
            }

            Card(
                Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(knock.to_room.id) },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Room: ${knock.to_room.roomNumber} ${knock.to_room.dorm} ",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Status: ${knock.status}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Sent: $createdAtFormatted",
                        style = MaterialTheme.typography.bodySmall
                    )
                    acceptedAtFormatted?.let { acc ->
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Accepted: $acc",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(Modifier.height(8.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        onAccept?.let { accept ->
                            IconButton(onClick = { accept(knock.id) }) {
                                Icon(Icons.Default.Done, contentDescription = "Accept")
                            }
                        }
                        onReject?.let { reject ->
                            IconButton(onClick = { reject(knock.id) }) {
                                Icon(Icons.Default.Close, contentDescription = "Reject")
                            }
                        }
                        if (onAccept == null && onReject == null) {
                            IconButton(onClick = { onCancel(knock.id) }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesScreen(
    navController: NavController,
    vm: KnockViewModel = hiltViewModel()
) {
    val sent by vm.sent.collectAsState()
    val received by vm.received.collectAsState()

    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("My Knocks", "Who's Knocking")

    LaunchedEffect(Unit) {
        vm.loadAll()
    }

    Column {
        TabRow(selectedTabIndex = tab) {
            tabs.forEachIndexed { i, title ->
                Tab(selected = i == tab, onClick = { tab = i }) {
                    Text(title, modifier = Modifier.padding(16.dp))
                }
            }
        }
        when (tab) {
            0 -> KnockList(
                knocks = sent,
                onItemClick = { id -> navController.navigate("detail/$id") },
                onCancel = { id -> vm.deleteKnock(id) }
            )
            1 -> KnockList(
                knocks = received,
                onItemClick = { id -> navController.navigate("detail/$id") },
                onCancel = {},
                onAccept = { id -> vm.acceptKnock(id) },
                onReject = { id -> vm.deleteKnock(id) }
            )
        }
    }
}