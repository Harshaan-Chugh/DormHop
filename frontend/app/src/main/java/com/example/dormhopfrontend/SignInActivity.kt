// app/src/main/java/com/example/dormhopfrontend/SignInActivity.kt
package com.example.dormhopfrontend

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dormhopfrontend.ui.theme.DormHopFrontendTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class SignInActivity : ComponentActivity() {
    companion object {
        const val RC_SIGN_IN = 1001
        const val KEY_ID_TOKEN = "key_id_token"
        private const val TAG = "SignInActivity"
    }

    private lateinit var googleSignInClient: GoogleSignInClient

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val idToken = account.idToken
                if (idToken != null) {
                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra(KEY_ID_TOKEN, idToken)
                    })
                } else {
                    Log.e(TAG, "No ID token retrieved from GoogleSignInAccount")
                    setResult(Activity.RESULT_CANCELED)
                }
            } catch (e: ApiException) {
                Log.e(TAG, "Google sign-in failed: ${e.statusCode}", e)
                setResult(Activity.RESULT_CANCELED)
            }
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // the google client id is found in res/values/strings.xml
        val serverClientId = getString(R.string.server_client_id)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(serverClientId)
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // 2) Launch the sign-in flow
        launcher.launch(googleSignInClient.signInIntent)
    }
}

@Composable
fun SignInScreen(onSignInClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = onSignInClick) {
            Text("Sign in with Google")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
    DormHopFrontendTheme {
        SignInScreen { }
    }
}

