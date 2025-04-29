package com.example.dormhopfrontend.screens

import android.app.Activity
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
 */
@Composable
fun RegistrationScreen(
    onTokenReceived: (String) -> Unit
) {
    val context = LocalContext.current
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
}

@Preview(showBackground = true)
@Composable
fun RegistrationScreenPreview() {
    DormHopFrontendTheme {
        RegistrationScreen(onTokenReceived = {})
    }
}
