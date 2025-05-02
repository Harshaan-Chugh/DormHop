package com.example.dormhopfrontend.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dormhopfrontend.viewmodel.MyPostingViewModel
import com.example.dormhopfrontend.viewmodel.MyPostingViewModel.*
import com.example.dormhopfrontend.model.RoomDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPostingScreen(
    onEdit: () -> Unit,
    vm: MyPostingViewModel = hiltViewModel()
) {
    val user     by vm.user.collectAsState()
    val loading  by vm.loading.collectAsState()
    val errorMsg by vm.error.collectAsState()

    LaunchedEffect(Unit) { vm.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Posting") },
                actions = {
                    // edit icon always visible – disable until data is loaded
                    IconButton(
                        onClick = onEdit,
                        enabled = user?.currentRoom != null
                    ) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
                }
            )
        }
    ) { inner ->
        when {
            loading ->
                Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

            errorMsg != null ->
                Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                    Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
                }

            user?.currentRoom == null ->
                Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
                    Text("You haven’t posted a room yet.")
                }

            else -> {
                // Re‑use the existing DetailScreen to render the card‑style view
                val room: RoomDto = user!!.currentRoom!!
                RoomDetailsContent(
                    room = room,
                    ownerName  = user!!.fullName,
                    ownerClass = user!!.classYear
                )
            }
        }
    }
}

