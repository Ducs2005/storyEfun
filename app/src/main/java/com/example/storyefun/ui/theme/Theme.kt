package com.example.storyefun.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

data class AppColors(
    val background: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val buttonBackground: Color,
    val buttonText: Color,
    val backgroundColor: Color,
    val tagColor: Color,
    val backgroundContrast1: Color,
    val backgroundContrast2: Color,
    val header: Color,
    val buttonOrange: Color,
    val backOrange: Color,
    val questCompletedColor: Color // Added for completed quests
)

// Light Theme
val LightColors = AppColors(
    background = Color.White,
    header = Color(0xFFFCE0CA), // Brighter orange
    textPrimary = Color.Black,
    textSecondary = Color.Gray,
    buttonBackground = Color.Red,
    buttonText = Color.White,
    backgroundColor = Color.White,
    tagColor = Color.DarkGray,
    backgroundContrast1 = Color.Black,
    backgroundContrast2 = Color(0xFF4D4D44),
    buttonOrange = Color(0xFFFFA500),
    backOrange = Color(0xFFFF7043),
    questCompletedColor = Color(0xFF22D079) // Light green for completed quests
)

// Dark Theme
val DarkColors = AppColors(
    background = Color.Black,
    header = Color(0xFF3B390A), // Deeper gray
    textPrimary = Color.White,
    textSecondary = Color.Gray,
    buttonBackground = Color.Red,
    buttonText = Color.Black,
    backgroundColor = Color.Black,
    tagColor = Color.Gray,
    backgroundContrast1 = Color.White,
    backgroundContrast2 = Color.Gray,
    buttonOrange = Color(0xFFFFA500),
    backOrange = Color(0xFFFF7043),
    questCompletedColor = Color(0xFF22D079) // Slightly darker light green for dark theme
)

// Create a Local variable to store colors
val LocalAppColors = staticCompositionLocalOf { LightColors }

// Function to apply theme
@Composable
fun AppTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colors = remember(darkTheme) {
        if (darkTheme) DarkColors else LightColors
    }

    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            content = content
        )
    }
}

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun ZalopayTheme(
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}