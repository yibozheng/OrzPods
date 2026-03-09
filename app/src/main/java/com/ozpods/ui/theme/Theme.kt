package com.ozpods.ui.theme
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1A73E8), onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E3FD), secondary = Color(0xFF5F6368),
    surface = Color(0xFFFFFBFE), background = Color(0xFFF8F9FA))
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8AB4F8), onPrimary = Color(0xFF003A70),
    primaryContainer = Color(0xFF004A8F), secondary = Color(0xFF9AA0A6),
    surface = Color(0xFF1E1E1E), background = Color(0xFF121212))
@Composable
fun OzPodsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
    MaterialTheme(colorScheme = colorScheme, content = content)
}
