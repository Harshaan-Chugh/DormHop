package com.example.dormhopfrontend.screens

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dormhopfrontend.R
import com.example.dormhopfrontend.SignInActivity
import com.example.dormhopfrontend.ui.theme.DormHopFrontendTheme
import kotlinx.coroutines.launch

/**
 * A simple registration screen that kicks off your
 * SignInActivity.doGoogleSignIn(...) flow when tapped.
 */
@Composable
fun RegistrationScreen(
    onTokenReceived: (String) -> Unit
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    var shouldRetry by remember { mutableStateOf(false) }

    // Launcher for the “Add Google Account” fallback
    val addAccountLauncher = rememberLauncherForActivityResult(
        contract = StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // User added a Google account → retry the flow
            shouldRetry = true
        }
    }

    // Retry once the user adds an account
    if (shouldRetry) {
        LaunchedEffect(Unit) {
            SignInActivity.doGoogleSignIn(
                context  = context,
                scope    = scope,
                launcher = addAccountLauncher,
                login    = onTokenReceived
            )
            shouldRetry = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color    = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier   = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Sign in",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                SignInActivity.doGoogleSignIn(
                                    context  = context,
                                    scope    = scope,
                                    launcher = addAccountLauncher,
                                    login    = onTokenReceived
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Sign in with Google")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrationScreenPreview() {
    DormHopFrontendTheme {
        RegistrationScreen(onTokenReceived = { /* no-op */ })
    }
}
