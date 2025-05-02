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
import com.example.dormhopfrontend.screens.CreateProfileScreen
import com.example.dormhopfrontend.screens.MyPostingScreen
import com.example.dormhopfrontend.screens.SavedScreen
import com.example.dormhopfrontend.screens.UpdatesScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DormHopFrontendTheme {
                val authVM: AuthViewModel = hiltViewModel()
                val googleIdToken by authVM.googleIdToken.collectAsState(initial = null)
                val jwt           by authVM.jwt.collectAsState(initial = null)
                val needsProfile  by authVM.needsProfile.collectAsState()

                when {
                    /* 1 ─ Sign-in flow */
                    googleIdToken == null ->
                        RegistrationScreen(authVM::onGoogleIdToken)

                    /* 2 ─ Exchange Google-token → JWT */
                    jwt == null ->
                        LoadingExchange(googleIdToken!!, authVM::exchangeForJwt)

                    /* 3 ─ Force them to finish profile if needed */
                    needsProfile ->
                        CreateProfileScreen(
                            onDone = { authVM.completeProfile() }
                        )

                    /* 4 ─ Otherwise show the real app */
                    else ->
                        MainScaffold(authVM = authVM)
                }
            }
        }
    }
}
/*──────────────── Helper composables ────────────────*/

/** Simple full-screen progress while we swap tokens. */
@Composable private fun LoadingExchange(idToken: String, run: (String)->Unit) {
    LaunchedEffect(idToken) { run(idToken) }
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        CircularProgressIndicator()
    }
}

/** The *real* scaffold once we have a JWT. */
@Composable
private fun MainScaffold(authVM: AuthViewModel) {
    val needsProfile by authVM.needsProfile.collectAsState()

    // 1) if they still need a profile, force them into it:
    if (needsProfile) {
        CreateProfileScreen(
            onDone = { authVM.completeProfile() }
        )
        return
    }

    // 2) otherwise show the normal app
    val navController = rememberNavController()
    val navBackStack  by navController.currentBackStackEntryAsState()
    val currentRoute  = navBackStack?.destination?.route

    Scaffold( bottomBar = {
        NavigationBar {
            bottomNavItems.forEach { item ->
                NavigationBarItem(
                    icon     = { Icon(item.icon, contentDescription = item.label) },
                    label    = { Text(item.label) },
                    selected = currentRoute == item.route,
                    onClick  = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                )
            }
        }
    }) { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {
            NavHost(navController, startDestination = "search") {
                composable("search") {
                    SearchScreen { room ->
                        navController.navigate("detail/${room.id}")
                    }
                }
                composable("updates") { UpdatesScreen() }
                //My Posting Screen
                composable("posting") {
                    MyPostingScreen(
                        onEdit = { navController.navigate("edit_profile") }   // open editor
                    )
                }
                //Create Profile Screen after clicking top right
                composable("edit_profile") {
                    CreateProfileScreen(
                        onDone = {
                            navController.popBackStack("posting", false)
                        }
                    )
                }
                composable("saved") {
                    SavedScreen { roomId ->
                        navController.navigate("detail/$roomId")
                    }
                }
                composable("detail/{roomId}",
                    arguments = listOf(navArgument("roomId") { type = NavType.IntType })
                ) { back ->
                    val id = back.arguments!!.getInt("roomId")
                    DetailScreen(id) { navController.popBackStack() }
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
