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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguageOption
import com.example.data.model.DockLayoutMode
import com.example.data.model.MaterialNavStyle
import com.example.data.model.ThemeMode
import com.example.data.model.UiStyleMode
import kotlin.math.abs

@Immutable
data class ThemeCustomizationTokens(
    val dynamicThemingEnabled: Boolean = true,
    val customAccentHex: Long = 0xFF6B57A8,
    val m3SurfaceDepth: Float = 1.0f,
    val m3CornerRadiusScale: Float = 1.0f,
    val m3ElevationScale: Float = 1.0f,
    val m3DensityScale: Float = 1.0f,
    val m3NavStyle: MaterialNavStyle = MaterialNavStyle.PILL_TONAL,
    val m3TypographyScale: Float = 1.0f,
    val m3MotionSpeed: Float = 1.0f,
    val glassBlurLevel: Float = 1.0f,
    val glassRefractionLevel: Float = 1.0f,
    val glassTransparencyLevel: Float = 1.0f,
    val glassLightResponse: Float = 1.0f,
    val glassSpecularHighlights: Float = 1.0f,
    val glassBorderSoftness: Float = 1.0f,
    val glassAdaptiveContrast: Float = 1.0f,
    val dockLayoutMode: DockLayoutMode = DockLayoutMode.AUTOMATIC,
    val useClassicClockHandles: Boolean = false,
    val language: AppLanguageOption = AppLanguageOption.ENGLISH
)

val LocalUiStyleMode = staticCompositionLocalOf { UiStyleMode.MATERIAL_3 }
val LocalDarkThemeFlag = staticCompositionLocalOf { false }
val LocalAmoledDarkFlag = staticCompositionLocalOf { false }
val LocalHapticsEnabled = staticCompositionLocalOf { true }
val LocalReducedMotionFlag = staticCompositionLocalOf { false }
val LocalGlassDepthIntensity = staticCompositionLocalOf { 1.0f }
val LocalThemeCustomization = staticCompositionLocalOf { ThemeCustomizationTokens() }

val BackdropCustomAccentSwatches: List<Pair<String, Long>> = listOf(
    "Periwinkle" to 0xFF6B57A8,
    "Ocean Cyan" to 0xFF2B7A9B,
    "Emerald Mint" to 0xFF2F856B,
    "Sunset Coral" to 0xFFC95E4B,
    "Amber Gold" to 0xFFB87D1E,
    "Berry Plum" to 0xFFA23E72,
    "Cobalt Steel" to 0xFF3E63A8,
    "Graphite Slate" to 0xFF4F5565
)

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

fun buildBackdropShapes(cornerScale: Float = 1.0f): Shapes {
    val s = cornerScale.coerceIn(0.5f, 1.5f)
    return Shapes(
        extraSmall = RoundedCornerShape((10f * s).dp),
        small = RoundedCornerShape((14f * s).dp),
        medium = RoundedCornerShape((20f * s).dp),
        large = RoundedCornerShape((28f * s).dp),
        extraLarge = RoundedCornerShape((34f * s).dp)
    )
}

val BackdropShapes = buildBackdropShapes(1.0f)

internal fun buildSeededColorScheme(
    seedHex: Long,
    isDark: Boolean,
    surfaceDepth: Float
): ColorScheme {
    val seed = Color(seedHex.toInt()).let {
        if (it.alpha < 0.9f) it.copy(alpha = 1f) else it
    }
    val depthBias = ((surfaceDepth - 1.0f) * 0.14f).coerceIn(-0.08f, 0.08f)
    return if (!isDark) {
        val primary = lerp(seed, Color(0xFF1F1C2C), 0.14f)
        val primaryContainer = lerp(seed, Color.White, 0.78f)
        val secondary = lerp(seed, PastelSage, 0.42f)
        val secondaryContainer = lerp(secondary, Color.White, 0.80f)
        val tintSurface = lerp(PastelAlabaster, seed, (0.04f + depthBias).coerceIn(0.01f, 0.12f))
        PastelLightColorScheme.copy(
            primary = primary,
            onPrimary = if (primary.luminance() > 0.55f) Color(0xFF16131E) else Color.White,
            primaryContainer = primaryContainer,
            onPrimaryContainer = lerp(seed, Color.Black, 0.72f),
            secondary = secondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = lerp(secondary, Color.Black, 0.72f),
            background = tintSurface,
            surface = tintSurface,
            surfaceContainerLow = lerp(tintSurface, seed, 0.05f),
            surfaceContainer = lerp(tintSurface, seed, 0.09f),
            surfaceContainerHigh = lerp(tintSurface, seed, 0.13f),
            surfaceContainerHighest = lerp(tintSurface, seed, 0.17f)
        )
    } else {
        val primary = lerp(seed, Color.White, 0.56f)
        val primaryContainer = lerp(seed, Color(0xFF161422), 0.52f)
        val secondary = lerp(seed, DarkPastelMint, 0.44f)
        val secondaryContainer = lerp(secondary, Color(0xFF121820), 0.62f)
        val baseSurface = lerp(TwilightObsidian, seed, (0.06f + depthBias).coerceIn(0.02f, 0.14f))
        PastelDarkColorScheme.copy(
            primary = primary,
            onPrimary = lerp(seed, Color.Black, 0.78f),
            primaryContainer = primaryContainer,
            onPrimaryContainer = lerp(seed, Color.White, 0.84f),
            secondary = secondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = lerp(secondary, Color.White, 0.84f),
            background = baseSurface,
            surface = baseSurface,
            surfaceContainerLow = lerp(DarkSurfaceLow, seed, 0.06f),
            surfaceContainer = lerp(DarkSurfaceContainer, seed, 0.09f),
            surfaceContainerHigh = lerp(DarkSurfaceHigh, seed, 0.12f),
            surfaceContainerHighest = lerp(DarkSurfaceHighest, seed, 0.15f)
        )
    }
}

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
    customization: ThemeCustomizationTokens = ThemeCustomizationTokens(dynamicThemingEnabled = useDynamicColor),
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val isGlass = uiStyle == UiStyleMode.FROSTED_GLASS_OS
    val effectiveDynamic = useDynamicColor && customization.dynamicThemingEnabled

    val context = LocalContext.current
    val rawScheme = when {
        effectiveDynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> {
            buildSeededColorScheme(
                seedHex = customization.customAccentHex,
                isDark = isDark,
                surfaceDepth = customization.m3SurfaceDepth
            )
        }
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

    val shapes = buildBackdropShapes(customization.m3CornerRadiusScale)
    val typography = buildAppTypography(customization.m3TypographyScale)

    CompositionLocalProvider(
        LocalUiStyleMode provides uiStyle,
        LocalDarkThemeFlag provides isDark,
        LocalAmoledDarkFlag provides (isDark && amoledDark),
        LocalHapticsEnabled provides hapticFeedbackEnabled,
        LocalReducedMotionFlag provides reducedMotionEnabled,
        LocalGlassDepthIntensity provides glassDepthIntensity.coerceIn(0.55f, 1.45f),
        LocalThemeCustomization provides customization
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
}
