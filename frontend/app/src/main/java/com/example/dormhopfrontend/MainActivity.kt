package com.example.dormhopfrontend

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dormhopfrontend.model.RoomDto
import com.example.dormhopfrontend.screens.DetailScreen
import com.example.dormhopfrontend.screens.RegistrationScreen
import com.example.dormhopfrontend.screens.SearchScreen
import com.example.dormhopfrontend.viewmodel.AuthViewModel
import com.example.dormhopfrontend.ui.theme.DormHopFrontendTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DormHopFrontendTheme {
                val authViewModel: AuthViewModel = hiltViewModel()
                val googleIdToken by authViewModel.googleIdToken.collectAsState(initial = null)
                val jwt           by authViewModel.jwt.collectAsState(initial = null)

                when {
                    // Phase 1: get Google ID token
                    googleIdToken == null -> RegistrationScreen { idToken ->
                        Toast.makeText(
                            this,
                            "🔑 Got Google ID token, length=${idToken.length}",
                            Toast.LENGTH_SHORT
                        ).show()
                        authViewModel.onGoogleIdToken(idToken)
                    }

                    // Phase 2: exchange to backend JWT
                    jwt == null -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                        LaunchedEffect(googleIdToken) {
                            authViewModel.exchangeForJwt(googleIdToken!!)
                        }
                    }

                    // Phase 3: real app
                    else -> {
                        val navController = rememberNavController()
                        val navBackStack by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStack?.destination?.route

                        Scaffold(
                            bottomBar = {
                                NavigationBar {
                                    bottomNavItems.forEach { item ->
                                        NavigationBarItem(
                                            icon =    { Icon(item.icon, contentDescription = item.label) },
                                            label =   { Text(item.label) },
                                            selected = currentRoute == item.route,
                                            onClick = {
                                                navController.navigate(item.route) {
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState   = true
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        ) { inner ->
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .padding(inner)
                            ) {
                                NavHost(navController, startDestination = "search") {
                                    composable("search") {
                                        SearchScreen { room: RoomDto ->
                                            navController.navigate("detail/${room.id}")
                                        }
                                    }
                                    composable("updates")  { PlaceholderScreen("Updates") }
                                    composable("posting")  { PlaceholderScreen("Your Posting") }
                                    composable("saved")    { PlaceholderScreen("Saved Dorms") }
                                    composable("inbox")    { PlaceholderScreen("Inbox") }
                                    composable(
                                        "detail/{roomId}",
                                        arguments = listOf(navArgument("roomId") { type = NavType.IntType })
                                    ) { back ->
                                        val roomId = back.arguments!!.getInt("roomId")
                                        DetailScreen(
                                            roomId = roomId,
                                            onBack = { navController.popBackStack() }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// bottom-nav items and PlaceholderScreen as before
private val bottomNavItems = listOf(
    BottomNavItem("search",  Icons.Default.Search,        "Search"),
    BottomNavItem("updates", Icons.Default.Notifications, "Updates"),
    BottomNavItem("posting", Icons.Default.PostAdd,      "Your Posting"),
    BottomNavItem("saved",   Icons.Default.Favorite,     "Saved Dorms"),
    BottomNavItem("inbox",   Icons.Default.Inbox,        "Inbox")
)

private data class BottomNavItem(val route: String, val icon: ImageVector, val label: String)

@Composable
private fun PlaceholderScreen(label: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$label Screen", style = MaterialTheme.typography.headlineSmall)
    }
}
