package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.model.ThemeMode
import com.example.data.model.UiStyleMode

val LocalUiStyleMode = staticCompositionLocalOf { UiStyleMode.MATERIAL_3 }
val LocalDarkThemeFlag = staticCompositionLocalOf { false }
val LocalAmoledDarkFlag = staticCompositionLocalOf { false }

private val PastelLightColorScheme = lightColorScheme(
    primary = PastelPeriwinkle,
    onPrimary = Color.White,
    primaryContainer = PastelPeriwinkleContainer,
    onPrimaryContainer = OnPastelPeriwinkleContainer,
    secondary = PastelSage,
    onSecondary = Color.White,
    secondaryContainer = PastelSageContainer,
    onSecondaryContainer = OnPastelSageContainer,
    tertiary = PastelApricot,
    onTertiary = Color.White,
    tertiaryContainer = PastelApricotContainer,
    onTertiaryContainer = OnPastelApricotContainer,
    background = PastelAlabaster,
    onBackground = LightInk,
    surface = PastelAlabaster,
    onSurface = LightInk,
    surfaceVariant = PastelSurfaceContainer,
    onSurfaceVariant = LightMutedInk,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = PastelSurfaceLow,
    surfaceContainer = PastelSurfaceContainer,
    surfaceContainerHigh = PastelSurfaceHigh,
    surfaceContainerHighest = PastelSurfaceHighest,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = PastelRoseError
)

private val PastelDarkColorScheme = darkColorScheme(
    primary = DarkPastelLilac,
    onPrimary = Color(0xFF281956),
    primaryContainer = DarkPastelLilacContainer,
    onPrimaryContainer = OnDarkPastelLilacContainer,
    secondary = DarkPastelMint,
    onSecondary = Color(0xFF0E382D),
    secondaryContainer = DarkPastelMintContainer,
    onSecondaryContainer = OnDarkPastelMintContainer,
    tertiary = DarkPastelPeach,
    onTertiary = Color(0xFF4A1B12),
    tertiaryContainer = DarkPastelPeachContainer,
    onTertiaryContainer = OnDarkPastelPeachContainer,
    background = TwilightObsidian,
    onBackground = DarkInk,
    surface = TwilightObsidian,
    onSurface = DarkInk,
    surfaceVariant = DarkSurfaceContainer,
    onSurfaceVariant = DarkMutedInk,
    surfaceContainerLowest = Color(0xFF0A090F),
    surfaceContainerLow = DarkSurfaceLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceHigh,
    surfaceContainerHighest = DarkSurfaceHighest,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = DarkRoseError
)

val BackdropShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

@Composable
fun BackdropTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    amoledDark: Boolean = false,
    uiStyle: UiStyleMode = UiStyleMode.MATERIAL_3,
    useDynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val baseScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> PastelDarkColorScheme
        else -> PastelLightColorScheme
    }

    val colorScheme = if (isDark && amoledDark) {
        baseScheme.copy(
            background = AmoledPitchBlack,
            surface = AmoledPitchBlack,
            surfaceContainerLowest = AmoledPitchBlack,
            surfaceContainerLow = Color(0xFF0A0A0E),
            surfaceContainer = Color(0xFF121118)
        )
    } else {
        baseScheme
    }

    CompositionLocalProvider(
        LocalUiStyleMode provides uiStyle,
        LocalDarkThemeFlag provides isDark,
        LocalAmoledDarkFlag provides (isDark && amoledDark)
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = BackdropShapes,
            content = content
        )
    }
}
