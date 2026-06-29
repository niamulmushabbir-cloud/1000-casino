package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
  darkColorScheme(
    primary = PurplePrimary,       // Lavender primary
    secondary = GoldPrimary,       // Gold accent
    tertiary = CrimsonRed,
    background = MatteBlack,
    surface = SurfaceCharcoal,
    onPrimary = PurpleActiveOn,
    onSecondary = MatteBlack,
    onTertiary = TextLight,
    onBackground = TextLight,
    onSurface = TextLight,
    surfaceVariant = SurfaceLighter,
    onSurfaceVariant = PurplePrimary
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for luxury casino vibe
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve branding
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
