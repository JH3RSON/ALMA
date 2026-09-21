package com.ima.alma.ui.theme

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

// Regla 1: Paleta de Capas iOS Modo Claro
private val LightColorScheme = lightColorScheme(
    primary = IosBlue,
    onPrimary = Color.White,
    background = FondoSistemaLight,
    onBackground = IosTextMainLight,
    surface = SuperficieLight,
    onSurface = IosTextMainLight,
    surfaceVariant = TarjetaReflexionLight,
    onSurfaceVariant = IosTextSecLight,
    outlineVariant = BordeSutilLight
)

// Regla 1: Paleta de Capas iOS Modo Oscuro
private val DarkColorScheme = darkColorScheme(
    primary = IosBlueDark,
    onPrimary = Color.White,
    background = FondoSistemaDark,
    onBackground = IosTextMainDark,
    surface = SuperficieDark,
    onSurface = IosTextMainDark,
    surfaceVariant = TarjetaReflexionDark,
    onSurfaceVariant = IosTextSecDark,
    outlineVariant = BordeSutilDark
)

@Composable
fun AlmaTheme(
    temaPreferido: String = "sistema",
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemInDark = isSystemInDarkTheme()

    val (colorScheme, isDarkStatus) = when (temaPreferido) {
        "ios_claro" -> LightColorScheme to false
        "ios_oscuro" -> DarkColorScheme to true
        "material_you" -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (systemInDark) {
                    dynamicDarkColorScheme(context) to true
                } else {
                    dynamicLightColorScheme(context) to false
                }
            } else {
                if (systemInDark) DarkColorScheme to true else LightColorScheme to false
            }
        }
        else -> { // "sistema" por defecto
            if (systemInDark) DarkColorScheme to true else LightColorScheme to false
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkStatus
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun ALMATheme(
    temaPreferido: String = "sistema",
    content: @Composable () -> Unit
) {
    AlmaTheme(temaPreferido = temaPreferido, content = content)
}
