package com.example.dormhopfrontend

import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.dormhopfrontend.screens.*
import com.example.dormhopfrontend.ui.theme.DormHopFrontendTheme
import com.example.dormhopfrontend.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DormHopFrontendTheme {
                val authVM: AuthViewModel = hiltViewModel()
                val googleIdToken by authVM.googleIdToken.collectAsState()
                val jwt           by authVM.jwt.collectAsState()
                val needsProfile  by authVM.needsProfile.collectAsState()

                when {
                    // 1) show google sign-in
                    googleIdToken == null ->
                        RegistrationScreen(authVM::onGoogleIdToken)

                    // 2) exchange for JWT
                    jwt == null ->
                        LoadingExchange(googleIdToken!!, authVM::exchangeForJwt)

                    // 3) force profile
                    needsProfile ->
                        CreateProfileScreen { authVM.completeProfile() }

                    // 4) real app
                    else ->
                        MainScaffold(authVM)
                }
            }
        }
    }
}

@Composable
private fun LoadingExchange(idToken: String, run: (String) -> Unit) {
    LaunchedEffect(idToken) { run(idToken) }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(authVM: AuthViewModel) {
    // once we have a JWT, re-check profile flag
    val needsProfile by authVM.needsProfile.collectAsState()
    if (needsProfile) {
        CreateProfileScreen { authVM.completeProfile() }
        return
    }

    val navController = rememberNavController()
    val backStack     by navController.currentBackStackEntryAsState()
    val currentRoute  = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = {
                            Text(
                                text = item.label,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        alwaysShowLabel = true
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "search",
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            composable("search") {
                SearchScreen { room ->
                    navController.navigate("detail/${room.id}")
                }
            }

            composable("updates") {
                UpdatesScreen()
            }

            composable("posting") {
                MyPostingScreen(
                    onEdit = { navController.navigate("edit_profile") }
                )
            }


            composable(
                "edit_profile"
            ) {
                CreateProfileScreen {
                    // go back to posting without creating a new stack entry
                    navController.popBackStack("posting", inclusive = false)
                }
            }

            composable("saved") {
                SavedScreen { roomId ->
                    navController.navigate("detail/$roomId")
                }
            }

            composable(
                route = "detail/{roomId}",
                arguments = listOf(navArgument("roomId") { type = NavType.IntType })
            ) { back ->
                val id = back.arguments!!.getInt("roomId")
                DetailScreen(id) { navController.popBackStack() }
            }

            composable("inbox") {
                InboxScreen()
            }
        }
    }
}

private data class BottomNavItem(val route: String, val icon: ImageVector, val label: String)
private val bottomNavItems = listOf(
    BottomNavItem("search",  Icons.Default.Search,        "Search"),
    BottomNavItem("updates", Icons.Default.Notifications, "Updates"),
    BottomNavItem("posting", Icons.Default.PostAdd,      "Posting"),
    BottomNavItem("saved",   Icons.Default.Favorite,     "Saved"),
    BottomNavItem("inbox",   Icons.Default.Inbox,        "Inbox")
)
