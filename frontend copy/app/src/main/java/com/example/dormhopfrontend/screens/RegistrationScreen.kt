package com.example.dormhopfrontend.screens

import android.app.Activity
<<<<<<< HEAD
import android.util.Log
import android.widget.Toast
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
=======
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dormhopfrontend.R
import com.example.dormhopfrontend.ui.theme.DormHopFrontendTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

/**
 * Registration screen that launches Google OAuth2.0 sign-in.
 * Once the ID token is obtained, it invokes [onTokenReceived].
>>>>>>> b415288ab12c93f049217ef3a1cbb40ddf906c9c
 */
@Composable
fun RegistrationScreen(
    onTokenReceived: (String) -> Unit
) {
    val context = LocalContext.current
<<<<<<< HEAD
    val scope   = rememberCoroutineScope()
    var shouldRetry by remember { mutableStateOf(false) }

    // Launcher for the “Add Google Account” fallback
    val addAccountLauncher = rememberLauncherForActivityResult(
        contract = StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // User added a Google account → retry the flow
            Toast
                .makeText(context, "✅ Account added, retrying sign-in…", Toast.LENGTH_SHORT)
                .show()
            shouldRetry = true
        }
    }

    // Retry once the user adds an account
    if (shouldRetry) {
        Toast
            .makeText(context, "🔄 Retrying Google Sign-In…", Toast.LENGTH_SHORT)
            .show()

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
                            Toast
                                .makeText(context, "👉 Starting Google Sign-In", Toast.LENGTH_SHORT)
                                .show()
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
=======
    // Configure GoogleSignIn to request ID token for your Web client ID
    val serverClientId = context.getString(R.string.server_client_id)
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(serverClientId)
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    // Launcher for the sign-in Intent
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val idToken = account.idToken
                if (idToken != null) {
                    // Log and toast for debugging
                    /*TODO: Delete this once fully implemented */
                    Log.d("RegistrationScreen", "Got ID token: $idToken")
                    Toast.makeText(context, "Got ID token!", Toast.LENGTH_SHORT).show()

                    onTokenReceived(idToken)
                } else {
                    Log.e("RegistrationScreen", "ID token was null")
                    Toast.makeText(context, "Didn't get ID token!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                // handle error (e.statusCode)
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = {
            //check if button is clicked
            /*TODO: delete the Toast later */
            Log.d("RegistrationScreen", "Button clicked!")
            Toast.makeText(context, "Button clicked!", Toast.LENGTH_SHORT).show()
            // 2) Then launch the Google Sign-In
            launcher.launch(googleSignInClient.signInIntent)
        }) {
            Text("Sign in with Google")
        }
    }
>>>>>>> b415288ab12c93f049217ef3a1cbb40ddf906c9c
}

@Preview(showBackground = true)
@Composable
fun RegistrationScreenPreview() {
    DormHopFrontendTheme {
<<<<<<< HEAD
        RegistrationScreen(onTokenReceived = { /* no-op */ })
=======
        RegistrationScreen(onTokenReceived = {})
>>>>>>> b415288ab12c93f049217ef3a1cbb40ddf906c9c
    }
}
