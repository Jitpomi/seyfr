package com.jitpomi.seyfr.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    secondary = PrimaryLightColor,
    tertiary = PrimaryColor
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFD9E4F5), // Soft light blue shade
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF173259), // Dark navy
    
    // Purging the purple! Making the whole app a clean monochromatic blue.
    secondary = PrimaryColor,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFFD9E4F5),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF173259),
    
    background = androidx.compose.ui.graphics.Color(0xFFF5F7FA), // Cool icy off-white to complement the blue
    onBackground = androidx.compose.ui.graphics.Color(0xFF1C1B1F),
    
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color(0xFF1C1B1F),
    
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFE2E4E9), // Cool crisp grey instead of muddy default
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF494A4F),
    
    outline = androidx.compose.ui.graphics.Color(0xFFC4C7CC),
    outlineVariant = androidx.compose.ui.graphics.Color(0xFFD9E4F5), // Soft blue for outline variants
    
    tertiary = PrimaryColor,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    tertiaryContainer = androidx.compose.ui.graphics.Color(0xFFD9E4F5),
    onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFF173259)

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun SeyfrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+, but we want to force our specific Primary/Secondary colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = androidx.compose.ui.graphics.Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
