package com.example.storyefun.ui.theme

import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.example.storyefun.R

// Define App Colors
data class AppColors(
    val background: Color,  // ✅ Đổi thành lambda @Composable
    val textPrimary: Color,
    val textSecondary: Color,
    val buttonBackground: Color,
    val buttonText: Color,
    val backgroundColor: Color,
    val tagColor: Color,
    val backgroundContrast1: Color,
    val backgroundContrast2: Color,
    val header: Color,
    val buttonOrange: Color

)

// Light Theme
val LightColors = AppColors(
//    background = { painterResource(id = R.drawable.background) },  // ✅ Gọi trong @Composable lambda
    background = Color.White,
    header = Color(0xFFF4A261),
    textPrimary = Color.Black,
    textSecondary = Color.Gray,
    buttonBackground = Color.Red,
    buttonText = Color.White,
    backgroundColor = Color.White,
    tagColor = Color.DarkGray,
    backgroundContrast1 = Color.Black,
    backgroundContrast2 = Color.DarkGray,
    buttonOrange = Color(0xFFFFA500)

)

// Dark Theme
val DarkColors = AppColors(
//    background = { painterResource(id = R.drawable.darkbackground) }, // ✅ Gọi trong @Composable lambda
    background = Color.Black,
    header = Color.Gray,
    textPrimary = Color.White,
    textSecondary = Color.Gray,
    buttonBackground = Color.Red,
    buttonText = Color.Black,
    backgroundColor = Color.Black,
    tagColor = Color.Gray,
    backgroundContrast1 = Color.White,
    backgroundContrast2 = Color.Gray,
    buttonOrange = Color(0xFFFFA500)
)

// Create a Local variable to store colors
val LocalAppColors = staticCompositionLocalOf { LightColors }

// Function to apply theme
@Composable
fun AppTheme(
    darkTheme: Boolean,  // Biến này quyết định dùng Light hay Dark theme
    content: @Composable () -> Unit
) {
    Log.d("App them upadte", " update")
    val colors = remember(darkTheme) {
        if (darkTheme) DarkColors else LightColors
    }

    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            content = content
        )
    }
}