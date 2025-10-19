package com.example.personalfinanceapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4CAF50), // button_dark_green
    onPrimary = Color.White,
    secondary = Color(0xFFF44336), // button_dark_red
    onSecondary = Color.White,
    background = Color.White, // white
    onBackground = Color.Black, // black
    surface = Color(0xFFF5F5F5), // light_gray
    onSurface = Color.Black, // black
    surfaceVariant = Color(0xFFF5F5F5) // light_gray
)

@Composable
fun PersonalFinanceAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}