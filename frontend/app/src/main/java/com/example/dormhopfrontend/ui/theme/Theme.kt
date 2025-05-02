package com.example.dormhopfrontend.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary       = RedPrimary,
    onPrimary     = White,
    secondary     = GoldAccent,
    onSecondary   = Black,
    background    = White,
    onBackground  = Black,
    surface       = White,
    onSurface     = Black
)

private val DarkColors = darkColorScheme(
    primary       = RedPrimary,
    onPrimary     = White,
    secondary     = GoldAccent,
    onSecondary   = Black,
    background    = Color(0xFF121212),
    onBackground  = White,
    surface       = Color(0xFF1E1E1E),
    onSurface     = White
)

@Composable
fun DormHopFrontendTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,  // turn off Android12 dynamic for consistency
    content: @Composable () -> Unit
) {
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColors
        else      -> LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography  = Typography,
        content     = content
    )
}
