package com.reelcounter.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ReelColors = darkColorScheme(
    primary = InstagramGradientPink,
    onPrimary = Color.White,
    secondary = InstagramGradientOrange,
    background = DarkBackground,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White
)

@Composable
fun ReelCounterTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ReelColors, content = content)
}
