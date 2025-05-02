package com.example.dormhopfrontend

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
<<<<<<< HEAD
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

@AndroidEntryPoint
=======
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

>>>>>>> b415288ab12c93f049217ef3a1cbb40ddf906c9c
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DormHopFrontendTheme {
<<<<<<< HEAD
                val authVM: AuthViewModel = hiltViewModel()

                /* ───────── auth state ───────── */
                val googleIdToken by authVM.googleIdToken.collectAsState(initial = null)
                val jwt           by authVM.jwt.collectAsState(initial = null)
                /*  Did the backend tell us the user is new?  */
                val needsProfile  by authVM.needsProfile.collectAsState(initial = false)
                /*  Optional: once we have a JWT we can also ask for /users/me
                    and mark needsProfile = (user.currentRoom == null)
                 */

                when {
                    /* 1 ─ Sign-in flow  */
                    googleIdToken == null -> RegistrationScreen(authVM::onGoogleIdToken)

                    /* 2 ─ Exchange Google-token → JWT  */
                    jwt == null -> LoadingExchange(googleIdToken!!, authVM::exchangeForJwt)

                    /* 3 ─ Real app UI  */
                    else -> MainScaffold(needsProfile)
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
private fun MainScaffold(needsProfile: Boolean) {
    val navController = rememberNavController()
    val navBackStack  by navController.currentBackStackEntryAsState()
    val currentRoute  = navBackStack?.destination?.route

    val startDest = remember(needsProfile) {
        if (needsProfile) "create_profile" else "search"
    }

    Scaffold(
        bottomBar = {
            if (currentRoute != "create_profile") {
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
=======
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
>>>>>>> b415288ab12c93f049217ef3a1cbb40ddf906c9c
                    }
                }
            }
        }
<<<<<<< HEAD
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {
            NavHost(navController, startDestination = startDest) {

                // forced-once screen
                composable("create_profile") {
                    CreateProfileScreen {
                        // after creation, go to search
                        navController.popBackStack("search", false)
                    }
                }

                composable("search") {
                    SearchScreen { room ->
                        navController.navigate("detail/${room.id}")
                    }
                }

                composable("updates")  { PlaceholderScreen("Updates") }

                // ⇨ YOUR POSTING now reuses CreateProfileScreen so they can edit
                composable("posting") {
                    CreateProfileScreen {
                        // after editing, back to search
                        navController.popBackStack("search", false)
                    }
                }

                composable("saved")    { PlaceholderScreen("Saved Dorms")  }
                composable("inbox")    { PlaceholderScreen("Inbox")       }

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
=======
    }
}

// 4) Bottom‐nav item definitions
private val bottomNavItems = listOf(
    BottomNavItem("search",  Icons.Default.Search,       "Search"),
    BottomNavItem("updates", Icons.Default.Notifications,"Updates"),
    BottomNavItem("posting", Icons.Default.PostAdd,     "Your Posting"),
    BottomNavItem("saved",   Icons.Default.Favorite,    "Saved Dorms"),
    BottomNavItem("inbox",   Icons.Default.Inbox,       "Inbox")
>>>>>>> b415288ab12c93f049217ef3a1cbb40ddf906c9c
)

private data class BottomNavItem(val route: String, val icon: ImageVector, val label: String)

<<<<<<< HEAD
=======
// 5) A quick placeholder for non‐implemented tabs
>>>>>>> b415288ab12c93f049217ef3a1cbb40ddf906c9c
@Composable
private fun PlaceholderScreen(label: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$label Screen", style = MaterialTheme.typography.headlineSmall)
    }
}
