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