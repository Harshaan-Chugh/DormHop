package com.example.dormhopfrontend.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dormhopfrontend.R
import com.example.dormhopfrontend.SignInActivity
import com.example.dormhopfrontend.ui.theme.GoldAccent
import com.example.dormhopfrontend.ui.theme.RedPrimary
import kotlinx.coroutines.launch

@Composable
fun RegistrationScreen(
    onTokenReceived: (String) -> Unit
) {
    val context        = LocalContext.current as Activity
    val scope          = rememberCoroutineScope()
    var shouldRetry    by remember { mutableStateOf(false) }

    // Launcher for “Add Google Account” fallback
    val addAccountLauncher = rememberLauncherForActivityResult(
        contract = StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, "✅ Account added, retrying…", Toast.LENGTH_SHORT).show()
            shouldRetry = true
        }
    }

    // If user just added an account, retry sign-in once
    if (shouldRetry) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "🔄 Retrying Google Sign-In…", Toast.LENGTH_SHORT).show()
            SignInActivity.doGoogleSignIn(
                context  = context,
                scope    = scope,
                launcher = addAccountLauncher,
                login    = onTokenReceived
            )
            shouldRetry = false
        }
    }

    // Full-screen red background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Logo at top
            Image(
                painter = painterResource(R.drawable.dormhop_logo),
                contentDescription = "DormHop Logo",
                modifier = Modifier
                    .size(300.dp)
                    .padding(bottom = 24.dp)
            )



            Spacer(Modifier.height(16.dp))

            // Optional subtitle
            Text(
                text = "",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(48.dp))

            // Google-SignIn button
            Button(
                onClick = {
                    Toast.makeText(context, "👉 Starting Google Sign-In", Toast.LENGTH_SHORT)
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp)
            ) {
                Text(
                    text = "Sign in with Google",
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}
