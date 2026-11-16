package com.example.techshop.ui

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


val BoardBg = Color(0xFF08231F)     // паяльная маска
val PanelColor = Color(0xFF0F332D)  // плоскость панели
val Copper = Color(0xFFE0A458)
val Trace = Color(0xFF7CCDB5)       // дорожки: вторичный акцент
val TraceDim = Color(0xFF2C6A5C)    // контуры и линии
val Silk = Color(0xFFE6F2ED)        // шелкография: основной текст
val SilkDim = Color(0xFF9CBDB2)     // второстепенный текст

val LedGreen = Color(0xFF4ADE80)
val LedAmber = Color(0xFFFFB020)
val LedRed = Color(0xFFFF6B5E)
val LedBlue = Color(0xFF4DA3FF)

val Chamfer = CutCornerShape(topEnd = 18.dp)
val ChamferSmall = CutCornerShape(topEnd = 12.dp, bottomStart = 12.dp)

private val Scheme = darkColorScheme(
    primary = Copper, onPrimary = Color(0xFF2A1B05),
    secondary = Trace, onSecondary = Color(0xFF04201A),
    background = BoardBg, onBackground = Silk,
    surface = PanelColor, onSurface = Silk,
    surfaceVariant = Color(0xFF164239), onSurfaceVariant = SilkDim,
    outline = TraceDim, outlineVariant = Color(0xFF1F4F44),
    primaryContainer = Color(0xFF3A2C12), onPrimaryContainer = Copper,
    secondaryContainer = Color(0xFF1D5448), onSecondaryContainer = Silk,
    error = LedRed, onError = Color(0xFF2A0A06),
    surfaceContainerLowest = BoardBg, surfaceContainerLow = Color(0xFF0C2B26),
    surfaceContainer = Color(0xFF123A33), surfaceContainerHigh = Color(0xFF174239),
    surfaceContainerHighest = Color(0xFF1C4A40)
)

private val BoardShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp), small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(10.dp), large = Chamfer,
    extraLarge = CutCornerShape(topEnd = 28.dp)
)

@Composable
fun TechShopTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, shapes = BoardShapes) {
        CompositionLocalProvider(LocalContentColor provides Scheme.onBackground, content = content)
    }
}
