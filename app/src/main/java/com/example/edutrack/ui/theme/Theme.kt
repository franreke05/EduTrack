package com.example.edutrack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = AcademicBlueDark,
    onPrimary = Color(0xFF0B2637),
    primaryContainer = Color(0xFF24445D),
    onPrimaryContainer = Color(0xFFD8ECF7),
    secondary = ScholarlyGreenDark,
    onSecondary = Color(0xFF12321D),
    secondaryContainer = Color(0xFF294B33),
    onSecondaryContainer = Color(0xFFD7EEDB),
    tertiary = WarmGoldDark,
    onTertiary = Color(0xFF402900),
    tertiaryContainer = Color(0xFF69490D),
    onTertiaryContainer = Color(0xFFFFE0A5),
    background = Night,
    onBackground = Color(0xFFE6ECEF),
    surface = NightSurface,
    onSurface = Color(0xFFE6ECEF),
    surfaceVariant = Color(0xFF22303A),
    onSurfaceVariant = Color(0xFFC0CAD1),
    outline = Color(0xFF82909A),
    outlineVariant = Color(0xFF34434D),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = AcademicBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8ECF7),
    onPrimaryContainer = Color(0xFF0D2B3C),
    secondary = ScholarlyGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDEBDD),
    onSecondaryContainer = Color(0xFF163520),
    tertiary = WarmGold,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE1AA),
    onTertiaryContainer = Color(0xFF3B2900),
    background = Paper,
    onBackground = Ink,
    surface = Color(0xFFFFFFFF),
    onSurface = Ink,
    surfaceVariant = Cloud,
    onSurfaceVariant = Slate,
    outline = Color(0xFF78858D),
    outlineVariant = Color(0xFFD2DAE0),
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val EdutrackShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

// Tema principal de la app.
@Composable
fun EduTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = EdutrackShapes,
        content = content
    )
}
