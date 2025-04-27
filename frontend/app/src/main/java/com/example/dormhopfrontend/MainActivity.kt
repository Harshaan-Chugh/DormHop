package com.example.dormhopfrontend

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.dormhopfrontend.ui.theme.DormHopFrontendTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DormHopFrontendTheme {
                // 1) Single NavController for everything
                val navController = rememberNavController()
                val navBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStack?.destination?.route

                // 2) Scaffold with bottom bar
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
                ) { innerPadding ->
                    Box(Modifier.padding(innerPadding)) {
                        // 3) NavHost uses that same controller
                        NavHost(navController, startDestination = "search") {
                            composable("search") {
                                SearchScreen { dorm ->
                                    val t = Uri.encode(dorm.title)
                                    val a = Uri.encode(dorm.address)
                                    navController.navigate("detail/$t/$a")
                                }
                            }
                            composable("updates")    { PlaceholderScreen("Updates")     }
                            composable("posting")    { PlaceholderScreen("Your Posting") }
                            composable("saved")      { PlaceholderScreen("Saved Dorms")  }
                            composable("inbox")      { PlaceholderScreen("Inbox")        }
                            composable(
                                "detail/{title}/{address}",
                                arguments = listOf(
                                    navArgument("title")   { type = NavType.StringType },
                                    navArgument("address") { type = NavType.StringType }
                                )
                            ) { back ->
                                val title   = back.arguments?.getString("title")   ?: ""
                                val address = back.arguments?.getString("address") ?: ""
                                DetailScreen(title, address) { navController.popBackStack() }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 4) Bottom‐nav item definitions
private val bottomNavItems = listOf(
    BottomNavItem("search",  Icons.Default.Search,       "Search"),
    BottomNavItem("updates", Icons.Default.Notifications,"Updates"),
    BottomNavItem("posting", Icons.Default.PostAdd,     "Your Posting"),
    BottomNavItem("saved",   Icons.Default.Favorite,    "Saved Dorms"),
    BottomNavItem("inbox",   Icons.Default.Inbox,       "Inbox")
)

private data class BottomNavItem(val route: String, val icon: ImageVector, val label: String)

// 5) A quick placeholder for non‐implemented tabs
@Composable
private fun PlaceholderScreen(label: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$label Screen", style = MaterialTheme.typography.headlineSmall)
    }
}
