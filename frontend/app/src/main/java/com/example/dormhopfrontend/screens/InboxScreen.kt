package com.example.dormhopfrontend.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dormhopfrontend.viewmodel.KnockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    knockVm: KnockViewModel = hiltViewModel()
) {
    val received by knockVm.received.collectAsState()
    val errorMsg by knockVm.error.collectAsState()
    val context  = LocalContext.current

    // load on first composition
    LaunchedEffect(Unit) {
        knockVm.loadAll()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Inbox") })
        }
    ) { inner ->
        Box(Modifier.padding(inner)) {
            if (errorMsg != null) {
                Text(
                    text = errorMsg!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(received) { knock ->
                    val other = knock.from_user
                    val isAccepted = knock.status == "accepted"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isAccepted) {
                                // launch email intent
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:${other.email}")
                                }
                                context.startActivity(intent)
                            },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                text = when {
                                    isAccepted -> "🎉 You’ve matched with ${other.fullName}!"
                                    else       -> "👋 Swap request from ${other.fullName}"
                                },
                                style = MaterialTheme.typography.titleMedium
                            )

                            if (isAccepted) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Tap to email: ${other.email}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { knockVm.acceptKnock(knock.id) }
                                    ) { Text("Accept") }
                                    OutlinedButton(
                                        onClick = { knockVm.deleteKnock(knock.id) }
                                    ) { Text("Decline") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
