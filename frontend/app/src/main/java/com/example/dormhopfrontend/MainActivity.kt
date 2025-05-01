package com.example.dormhopfrontend

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

                //track token
                val idToken by authViewModel.idToken.collectAsState(initial = null)

                if (idToken == null) {
                    // 1) Show sign-in screen until we have a token
                    RegistrationScreen { token ->
                        authViewModel.setIdToken(token)
                    }
                } else {
                    // 2) Once signed in, show main app UI
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
                    ) { innerPadding ->
                        Box(Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                        ) {
                            NavHost(navController, startDestination = "search") {
                                composable("search") {
                                    SearchScreen { dorm ->
                                        val t = Uri.encode(dorm.title)
                                        val a = Uri.encode(dorm.address)
                                        navController.navigate("detail/$t/$a")
                                    }
                                }
                                composable("updates")  { PlaceholderScreen("Updates")     }
                                composable("posting")  { PlaceholderScreen("Your Posting") }
                                composable("saved")    { PlaceholderScreen("Saved Dorms")  }
                                composable("inbox")    { PlaceholderScreen("Inbox")       }
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
