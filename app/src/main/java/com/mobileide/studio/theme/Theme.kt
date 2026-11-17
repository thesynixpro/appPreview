package com.mobileide.studio.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF2E2A3A),
    onPrimaryContainer = Color(0xFFEAEAEF),
    inversePrimary = Color(0xFF7E57C2),
    secondary = Color(0xFF3AA9FF),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF2A2A30),
    onSecondaryContainer = Color(0xFFEAEAEF),
    tertiary = Color(0xFFC3B1FA),
    onTertiary = Color(0xFF3A1E70),
    tertiaryContainer = Color(0xFF4A3A6A),
    onTertiaryContainer = Color(0xFFEAEAEF),
    background = Color(0xFF121214),
    onBackground = Color(0xFFEAEAEF),
    surface = Color(0xFF1E1E22),
    onSurface = Color(0xFFEAEAEF),
    surfaceVariant = Color(0xFF2A2A30),
    onSurfaceVariant = Color(0xFFB9B9C0),
    surfaceTint = Color(0xFFBB86FC),
    surfaceBright = Color(0xFF1E1E22),
    surfaceDim = Color(0xFF121214),
    surfaceContainer = Color(0xFF2A2A30),
    surfaceContainerHigh = Color(0xFF35353D),
    surfaceContainerHighest = Color(0xFF41414A),
    surfaceContainerLow = Color(0xFF1A1A1E),
    surfaceContainerLowest = Color(0xFF121214),
    error = Color(0xFFCF6679),
    onError = Color(0xFF000000),
    errorContainer = Color(0xFF3A1A1A),
    onErrorContainer = Color(0xFFFFDADA),
    outline = Color(0xFF3A3A42),
    outlineVariant = Color(0xFF2A2A30),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFEAEAEF),
    inverseOnSurface = Color(0xFF121214),
)

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6A4C93),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF2E86C0),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFF5F5F7),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    outline = Color(0xFF79747E),
)

/** Editor / terminal monospace style shared by the code editor and terminal. */
val MonoTextStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontSize = 13.sp,
    color = Color(0xFFEAEAEF),
)

val MobileIDETypography = Typography(
    headlineLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 30.sp),
    headlineMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp),
    headlineSmall = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp),
    titleLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp),
    titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp),
)

@Composable
fun MobileIDETheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = MobileIDETypography,
        content = content
    )
}
