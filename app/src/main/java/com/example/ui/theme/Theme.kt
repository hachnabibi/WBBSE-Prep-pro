package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = SecondaryEmeraldLight,
    onSecondary = Color.Black,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = SecondaryContainerLight,
    tertiary = TertiaryAmberLight,
    onTertiary = Color.Black,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = TertiaryContainerLight,
    background = DarkBackground,
    onBackground = Color(0xFFF8FAFC),
    surface = DarkSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155),
    error = WrongRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = Color(0xFF312E81),
    secondary = SecondaryEmerald,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFECFDF5),
    onSecondaryContainer = Color(0xFF064E3B),
    tertiary = TertiaryAmber,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFFBEB),
    onTertiaryContainer = Color(0xFF78350F),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    error = WrongRed,
    onError = Color.White
)

@Composable
fun DeshExamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontSizeScale: Float = 1.0f,
    dynamicColor: Boolean = false, // Set to false to keep rich DeshExam academic brand identity
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
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                // Set light status bars so mobile icons (time, wifi, battery) are dark in light theme
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    val currentDensity = LocalDensity.current
    val customDensity = remember(currentDensity.density, currentDensity.fontScale, fontSizeScale) {
        Density(
            density = currentDensity.density,
            fontScale = currentDensity.fontScale * fontSizeScale.coerceIn(0.8f, 1.4f)
        )
    }

    CompositionLocalProvider(
        LocalDensity provides customDensity
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
