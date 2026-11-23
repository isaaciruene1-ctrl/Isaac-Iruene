package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.model.ThemeMode
import com.example.data.model.UiStyleMode
import kotlin.math.abs

val LocalUiStyleMode = staticCompositionLocalOf { UiStyleMode.MATERIAL_3 }
val LocalDarkThemeFlag = staticCompositionLocalOf { false }
val LocalAmoledDarkFlag = staticCompositionLocalOf { false }
val LocalHapticsEnabled = staticCompositionLocalOf { true }
val LocalReducedMotionFlag = staticCompositionLocalOf { false }
val LocalGlassDepthIntensity = staticCompositionLocalOf { 1.0f }

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

/**
 * Guarantees WCAG-grade foreground/background separation across dynamic Material You dark palettes
 * when layered on translucent Frosted Glass OS panels and AMOLED pitch-black backdrops.
 */
internal fun ensureDarkGlassContrastScheme(
    scheme: ColorScheme,
    amoledDark: Boolean,
    isGlass: Boolean
): ColorScheme {
    fun needsLightContentFix(container: Color, content: Color): Boolean {
        return abs(container.luminance() - content.luminance()) < 0.34f
    }

    val safePrimary = if (scheme.primary.luminance() < 0.36f) DarkPastelLilac else scheme.primary
    val safeOnPrimary = if (needsLightContentFix(safePrimary, scheme.onPrimary)) Color(0xFF1F1246) else scheme.onPrimary
    val safePrimaryContainer = if (scheme.primaryContainer.luminance() > 0.42f || scheme.primaryContainer.luminance() < 0.04f) {
        DarkPastelLilacContainer
    } else {
        scheme.primaryContainer
    }
    val safeOnPrimaryContainer = if (needsLightContentFix(safePrimaryContainer, scheme.onPrimaryContainer) || scheme.onPrimaryContainer.luminance() < 0.65f) {
        OnDarkPastelLilacContainer
    } else {
        scheme.onPrimaryContainer
    }

    val safeSecondary = if (scheme.secondary.luminance() < 0.36f) DarkPastelMint else scheme.secondary
    val safeSecondaryContainer = if (scheme.secondaryContainer.luminance() > 0.42f || scheme.secondaryContainer.luminance() < 0.04f) {
        DarkPastelMintContainer
    } else {
        scheme.secondaryContainer
    }
    val safeOnSecondaryContainer = if (needsLightContentFix(safeSecondaryContainer, scheme.onSecondaryContainer) || scheme.onSecondaryContainer.luminance() < 0.65f) {
        OnDarkPastelMintContainer
    } else {
        scheme.onSecondaryContainer
    }

    val baseDark = scheme.copy(
        primary = safePrimary,
        onPrimary = safeOnPrimary,
        primaryContainer = safePrimaryContainer,
        onPrimaryContainer = safeOnPrimaryContainer,
        secondary = safeSecondary,
        secondaryContainer = safeSecondaryContainer,
        onSecondaryContainer = safeOnSecondaryContainer,
        onBackground = DarkInk,
        onSurface = DarkInk,
        onSurfaceVariant = DarkMutedInk,
        outline = DarkOutline,
        outlineVariant = if (isGlass) Color(0xFF6C6485) else DarkOutlineVariant
    )

    return if (amoledDark) {
        baseDark.copy(
            background = AmoledPitchBlack,
            surface = AmoledPitchBlack,
            surfaceContainerLowest = AmoledPitchBlack,
            surfaceContainerLow = Color(0xFF100F18),
            surfaceContainer = Color(0xFF181724),
            surfaceContainerHigh = Color(0xFF232132),
            surfaceContainerHighest = Color(0xFF302E42)
        )
    } else if (isGlass) {
        baseDark.copy(
            surfaceContainerLow = Color(0xFF191724),
            surfaceContainer = Color(0xFF211E30),
            surfaceContainerHigh = Color(0xFF2B283D),
            surfaceContainerHighest = Color(0xFF37334C)
        )
    } else {
        baseDark
    }
}

@Composable
fun BackdropTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    amoledDark: Boolean = false,
    uiStyle: UiStyleMode = UiStyleMode.MATERIAL_3,
    useDynamicColor: Boolean = true,
    hapticFeedbackEnabled: Boolean = true,
    reducedMotionEnabled: Boolean = false,
    glassDepthIntensity: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val isGlass = uiStyle == UiStyleMode.FROSTED_GLASS_OS

    val context = LocalContext.current
    val rawScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> PastelDarkColorScheme
        else -> PastelLightColorScheme
    }

    val colorScheme = if (isDark) {
        ensureDarkGlassContrastScheme(
            scheme = rawScheme,
            amoledDark = amoledDark,
            isGlass = isGlass
        )
    } else {
        rawScheme
    }

    CompositionLocalProvider(
        LocalUiStyleMode provides uiStyle,
        LocalDarkThemeFlag provides isDark,
        LocalAmoledDarkFlag provides (isDark && amoledDark),
        LocalHapticsEnabled provides hapticFeedbackEnabled,
        LocalReducedMotionFlag provides reducedMotionEnabled,
        LocalGlassDepthIntensity provides glassDepthIntensity.coerceIn(0.55f, 1.45f)
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = BackdropShapes,
            content = content
        )
    }
}
