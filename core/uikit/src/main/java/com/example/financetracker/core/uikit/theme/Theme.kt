package com.example.financetracker.core.uikit.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimarySoft,
    onPrimaryContainer = PrimaryStrong,

    background = SurfaceBgLight,        // фирменный светлый фон экрана
    onBackground = InkPrimary,          // темный текст на фоне экрана

    surface = SurfaceLight,             // белый фон карточек
    onSurface = InkPrimary,             // основной текст на карточках
    onSurfaceVariant = InkSecondary,    // второстепенный текст и подписи

    surfaceVariant = Surface2Light,     // мягкие подложки
    outline = Line                      // границы
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryStrong,
    onPrimaryContainer = PrimarySoft,

    background = SurfaceBgDark,         // глубокий темный фон
    onBackground = InkPrimaryDark,      // светлый текст на фоне

    surface = SurfaceDark,              // темный фон карточек
    onSurface = InkPrimaryDark,         // светлый текст карточки
    onSurfaceVariant = InkSecondaryDark,// приглушенный текст в темноте

    surfaceVariant = Surface2Dark,
    outline = LineDark

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun FinancetrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}