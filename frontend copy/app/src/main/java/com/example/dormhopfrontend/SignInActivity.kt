<<<<<<< HEAD
package com.example.dormhopfrontend

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialOption
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignInActivity {
    companion object {
        /**
         * Launches the credential flow. On success, calls [login] with the ID token.
         * If no saved credential, fires [launcher] to let the user add a Google account.
         */
        fun doGoogleSignIn(
            context: Context,
            scope: CoroutineScope,
            launcher: ManagedActivityResultLauncher<Intent, ActivityResult>?,
            login: (String) -> Unit
        ) {
            val credentialManager = CredentialManager.create(context)
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(getCredentialOptions(context))
                .build()

            scope.launch {
                try {
                    val response = credentialManager.getCredential(context, request)
                    Toast
                        .makeText(context, "🔍 Got credential response", Toast.LENGTH_SHORT)
                        .show()
                    // response.credential can be a GoogleIdTokenCredential or a CustomCredential
                    val idToken: String? = when (val cred = response.credential) {
                        is GoogleIdTokenCredential -> cred.idToken
                        is CustomCredential -> {
                            // in case older API still wraps it
                            GoogleIdTokenCredential.createFrom(cred.data).idToken
                        }
                        else -> null
                    }

                    if (idToken != null) {
                        Toast
                            .makeText(context, "✅ ID token retrieved", Toast.LENGTH_SHORT)
                            .show()
                        // Exchange with Firebase
                        val firebaseCred = GoogleAuthProvider.getCredential(idToken, null)
                        FirebaseAuth.getInstance().signInWithCredential(firebaseCred).await()
                        // Finally call your callback so the UI can move on
                        login(idToken)
                        Log.d("SignInActivity", "raw Google ID token: $idToken")
                    } else {
                        Toast
                            .makeText(context, "⚠️ No ID token in credential", Toast.LENGTH_LONG)
                            .show()
                        Log.e(TAG, "Got credential but no ID token")
                    }
                } catch (e: NoCredentialException) {
                    Toast
                        .makeText(context, "➕ No credential found; launching Add-Account…", Toast.LENGTH_LONG)
                        .show()
                    launcher?.launch(getAddAccountIntent())
                } catch (e: GetCredentialException) {
                    Toast
                        .makeText(context, "❌ Credential request failed: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                    Log.e(TAG, "Credential request failed", e)
                }
            }

        }

        private fun getAddAccountIntent(): Intent {
            return Intent(Settings.ACTION_ADD_ACCOUNT).apply {
                putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
            }
        }

        private fun getCredentialOptions(context: Context): CredentialOption {
            return GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .setServerClientId(context.getString(R.string.server_client_id))
                .build()
        }
    }
}
=======
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

>>>>>>> b415288ab12c93f049217ef3a1cbb40ddf906c9c
