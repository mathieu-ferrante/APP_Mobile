package com.phoenix.fitpro.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Dark Color Scheme (default) ───────────────────────────────────────────────
private val PhoenixDarkColorScheme = darkColorScheme(
    primary                = ElectricBlue,
    onPrimary              = Color.White,
    primaryContainer       = ElectricBlueAlpha15,
    onPrimaryContainer     = ElectricBlueLight,

    secondary              = NeonGreen,
    onSecondary            = Color.Black,
    secondaryContainer     = NeonGreenAlpha15,
    onSecondaryContainer   = NeonGreenLight,

    tertiary               = AccentOrange,
    onTertiary             = Color.White,
    tertiaryContainer      = AccentOrangeAlpha15,
    onTertiaryContainer    = AccentOrangeLight,

    error                  = ErrorRed,
    onError                = Color.White,

    background             = DeepNavy,
    onBackground           = TextPrimary,

    surface                = DarkSurface,
    onSurface              = TextPrimary,
    surfaceVariant         = DarkSurfaceVariant,
    onSurfaceVariant       = TextSecondary,

    outline                = DarkOutline,
    outlineVariant         = DarkOutline.copy(alpha = 0.5f),

    inverseSurface         = TextPrimary,
    inverseOnSurface       = DeepNavy,
    inversePrimary         = ElectricBlueDark,
)

// ── Light Color Scheme (accessible but not default) ───────────────────────────
private val PhoenixLightColorScheme = lightColorScheme(
    primary                = ElectricBlueDark,
    onPrimary              = Color.White,
    primaryContainer       = ElectricBlueAlpha15,
    onPrimaryContainer     = ElectricBlueDark,
    secondary              = NeonGreenDark,
    onSecondary            = Color.White,
    background             = Color(0xFFF8FAFF),
    onBackground           = Color(0xFF0A0E1A),
    surface                = Color.White,
    onSurface              = Color(0xFF0A0E1A),
)

@Composable
fun PhoenixTheme(
    darkTheme: Boolean = true, // Default to dark for premium feel
    dynamicColor: Boolean = false, // Keep custom palette by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> PhoenixDarkColorScheme
        else      -> PhoenixLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DeepNavy.toArgb()
            window.navigationBarColor = DeepNavy.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = PhoenixTypography,
        shapes      = PhoenixShapes,
        content     = content
    )
}
