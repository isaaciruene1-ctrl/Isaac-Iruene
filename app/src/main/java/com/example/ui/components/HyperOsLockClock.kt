package com.example.ui.components

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.FontRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.UiStyleMode
import com.example.engine.depth.ClockHorizontalAlign
import com.example.ui.screens.CanvasLookFilter
import com.example.ui.theme.AppMotion
import com.example.ui.theme.ArchivoBlackFontFamily
import com.example.ui.theme.BebasNeueFontFamily
import com.example.ui.theme.BigShouldersFontFamily
import com.example.ui.theme.BodoniModaFontFamily
import com.example.ui.theme.CaveatFontFamily
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.CormorantFontFamily
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.InstrumentSerifFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalReducedMotionFlag
import com.example.ui.theme.LocalThemeCustomization
import com.example.ui.theme.LocalUiStyleMode
import com.example.ui.theme.MajorMonoFontFamily
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.OswaldFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlayfairFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.RighteousFontFamily
import com.example.ui.theme.ShantellSansFontFamily
import com.example.ui.theme.SilkscreenFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.SyneFontFamily
import com.example.ui.theme.SyneMonoFontFamily
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ClockAspectProfile(val label: String) {
    TALL_VERTICAL("Tall Vertical"),
    BALANCED("Balanced"),
    WIDE_HORIZONTAL("Wide Horizontal")
}

enum class ClockSignatureMode(val label: String) {
    DATE_WEATHER("Date & Weather"),
    WEEKDAY_SECONDS("Day • Seconds"),
    MINIMAL_DATE("Minimal Date"),
    EDITORIAL_TAG("Editorial"),
    PURE_TIME("Numerals Only")
}

enum class HyperOsClockFamily(val label: String, val subtitle: String) {
    CLASSIC("Classic", "Clean stretchable standard & utility clocks"),
    RHOMBUS("Rhombus", "Geometric dual-tone & multi-row frames"),
    MAGAZINE("Magazine", "Poster editorial numerals for depth overlap")
}

enum class ClockTypefaceCategory(val label: String) {
    SANS("Sans"),
    SERIF("Serif"),
    ROUNDED("Rounded"),
    MONO("Mono"),
    SCRIPT("Hand")
}

enum class HyperOsClockFace(
    val label: String,
    val subtitle: String,
    val recommendedFont: HyperOsClockFont,
    val family: HyperOsClockFamily,
    val aspectProfile: ClockAspectProfile
) {
    // MAGAZINE & SHOWCASE EDITORIAL
    POSTER_SPLIT("Poster Bold", "Tall editorial display numerals", HyperOsClockFont.BIG_SHOULDERS, HyperOsClockFamily.MAGAZINE, ClockAspectProfile.WIDE_HORIZONTAL),
    TALL_POSTER_1340("Tall 1340", "Ultra-condensed full-bleed canyon numerals", HyperOsClockFont.BEBAS_NEUE, HyperOsClockFamily.MAGAZINE, ClockAspectProfile.BALANCED),
    CANYON_MONUMENT("Canyon Stack", "Oversized rounded canyon depth stack", HyperOsClockFont.OUTFIT, HyperOsClockFamily.MAGAZINE, ClockAspectProfile.TALL_VERTICAL),
    LUXE_VOGUE("Luxe Vogue", "Tight condensed high-fashion serif", HyperOsClockFont.CORMORANT, HyperOsClockFamily.MAGAZINE, ClockAspectProfile.BALANCED),
    ROMAN_MONUMENT("Monument", "Sculpted classical serif with stone rule", HyperOsClockFont.CINZEL, HyperOsClockFamily.MAGAZINE, ClockAspectProfile.WIDE_HORIZONTAL),
    EDITORIAL_DUO("Editorial Duo", "High-fashion stacked serif with accent bar", HyperOsClockFont.BODONI_MODA, HyperOsClockFamily.MAGAZINE, ClockAspectProfile.TALL_VERTICAL),
    NEON_OVERLAP("Glass Overlap", "Layered translucent depth digits with slice", HyperOsClockFont.ARCHIVO_BLACK, HyperOsClockFamily.MAGAZINE, ClockAspectProfile.TALL_VERTICAL),
    SPIRE_MONOLITH("Spire Tower", "Ultra-tall architectural horizon digits", HyperOsClockFont.BIG_SHOULDERS, HyperOsClockFamily.MAGAZINE, ClockAspectProfile.BALANCED),
    AQUA_MIST_TALL("Aqua Glass", "Slender elongated stacked numerals", HyperOsClockFont.SPACE_GROTESK, HyperOsClockFamily.MAGAZINE, ClockAspectProfile.TALL_VERTICAL),
    HORIZON_SILK("Horizon Silk", "Broad luminous curved glass readout", HyperOsClockFont.FREDOKA, HyperOsClockFamily.CLASSIC, ClockAspectProfile.WIDE_HORIZONTAL),
    MEADOW_BLOCK("Meadow Block", "Compact heavy stacked block digits", HyperOsClockFont.OSWALD, HyperOsClockFamily.MAGAZINE, ClockAspectProfile.TALL_VERTICAL),
    MAGAZINE_STACKED("Editorial Stack", "Stacked bold cover typography", HyperOsClockFont.OSWALD, HyperOsClockFamily.MAGAZINE, ClockAspectProfile.TALL_VERTICAL),
    ATELIER_SERIF("Vogue Serif", "High-fashion editorial serif numerals", HyperOsClockFont.PLAYFAIR, HyperOsClockFamily.MAGAZINE, ClockAspectProfile.BALANCED),
    CALLIGRAPHY_SEAL("Kanso Seal", "Vertical numerals & vermilion stamp", HyperOsClockFont.CORMORANT, HyperOsClockFamily.MAGAZINE, ClockAspectProfile.TALL_VERTICAL),

    // CLASSIC & ARCHITECTURAL TELEMETRY
    CLASSIC_HORIZON("Horizon Minimal", "Clean stretchable lock clock", HyperOsClockFont.OUTFIT, HyperOsClockFamily.CLASSIC, ClockAspectProfile.WIDE_HORIZONTAL),
    CLASSIC_UTILITY("Daylight Pill", "Date capsule, weather & signature", HyperOsClockFont.JAKARTA, HyperOsClockFamily.CLASSIC, ClockAspectProfile.WIDE_HORIZONTAL),
    SWISS_GRID_SPLIT("Swiss Grid", "Hairline grid split with footer rule", HyperOsClockFont.JAKARTA, HyperOsClockFamily.CLASSIC, ClockAspectProfile.WIDE_HORIZONTAL),
    CHRONO_TELEMETRY("Chrono Bar", "Tabular time & live 60s progress bar", HyperOsClockFont.JETBRAINS_MONO, HyperOsClockFamily.CLASSIC, ClockAspectProfile.WIDE_HORIZONTAL),
    KINETIC_MONO("Precision Mono", "Tabular numerals & live seconds", HyperOsClockFont.JETBRAINS_MONO, HyperOsClockFamily.CLASSIC, ClockAspectProfile.WIDE_HORIZONTAL),
    RETRO_PIXEL_MATRIX("Matrix LED", "Crisp 8-bit LED numerals & telemetry dot", HyperOsClockFont.SILKSCREEN, HyperOsClockFamily.CLASSIC, ClockAspectProfile.WIDE_HORIZONTAL),
    SOLAR_ARC_GAUGE("Solar Horizon", "Curved zenith daylight arc over serif time", HyperOsClockFont.INSTRUMENT_SERIF, HyperOsClockFamily.CLASSIC, ClockAspectProfile.WIDE_HORIZONTAL),
    ANALOG_MINIMAL("Chronograph", "Sweeping hands & minimalist dial", HyperOsClockFont.SPACE_GROTESK, HyperOsClockFamily.CLASSIC, ClockAspectProfile.BALANCED),
    ORBITAL_RING_HYBRID("Orbital Hybrid", "Analog dial ticks orbiting digital core", HyperOsClockFont.ORBITRON, HyperOsClockFamily.CLASSIC, ClockAspectProfile.BALANCED),
    COMPASS_CHRONO("Compass Dial", "Precision azimuth dial orbiting mono core", HyperOsClockFont.SYNE_MONO, HyperOsClockFamily.CLASSIC, ClockAspectProfile.BALANCED),

    // RHOMBUS & MODULAR CAPSULES
    GLASS_PILL_TRIO("Capsule Trio", "Modular frosted hour, min & status pills", HyperOsClockFont.SYNE, HyperOsClockFamily.RHOMBUS, ClockAspectProfile.WIDE_HORIZONTAL),
    BRUTALIST_SLAB("Slab Split", "Heavy asymmetric slab blocks & battery pill", HyperOsClockFont.ARCHIVO_BLACK, HyperOsClockFamily.RHOMBUS, ClockAspectProfile.WIDE_HORIZONTAL),
    KINETIC_STEP("Step Cascade", "Staggered diagonal hour/minute geometry", HyperOsClockFont.RIGHTEOUS, HyperOsClockFamily.RHOMBUS, ClockAspectProfile.TALL_VERTICAL),
    HANDWRITTEN_STAMP("Studio Script", "Expressive brush numerals & date stamp", HyperOsClockFont.CAVEAT, HyperOsClockFamily.RHOMBUS, ClockAspectProfile.BALANCED),
    RHOMBUS_PRISM("Prism Frame", "Faceted geometric rhombus frame", HyperOsClockFont.ORBITRON, HyperOsClockFamily.RHOMBUS, ClockAspectProfile.BALANCED),
    RHOMBUS_DUAL_ROW("Split Prism", "Dual-tone hours with diamond divider", HyperOsClockFont.SYNE, HyperOsClockFamily.RHOMBUS, ClockAspectProfile.BALANCED),
    PIXEL_DUAL_DIAL("Orbit Dial", "Stacked accent numerals & sec arc", HyperOsClockFont.SPACE_GROTESK, HyperOsClockFamily.RHOMBUS, ClockAspectProfile.BALANCED),
    DOODLE_PLAY("Kinetic Ribbon", "Playful curved strokes & wave motif", HyperOsClockFont.FREDOKA, HyperOsClockFamily.RHOMBUS, ClockAspectProfile.BALANCED),
    CUTOUT_POP("Tilted Tiles", "Angled frosted numeral plaques", HyperOsClockFont.SHANTELL_SANS, HyperOsClockFamily.RHOMBUS, ClockAspectProfile.BALANCED)
}

enum class HyperOsClockFont(
    val label: String,
    val fontFamily: FontFamily,
    @param:FontRes val resId: Int,
    val category: ClockTypefaceCategory
) {
    BIG_SHOULDERS("Poster Bold", BigShouldersFontFamily, R.font.big_shoulders_display, ClockTypefaceCategory.SANS),
    BEBAS_NEUE("Bebas", BebasNeueFontFamily, R.font.bebas_neue, ClockTypefaceCategory.SANS),
    ARCHIVO_BLACK("Archivo", ArchivoBlackFontFamily, R.font.archivo_black, ClockTypefaceCategory.SANS),
    OUTFIT("Outfit", OutfitFontFamily, R.font.outfit, ClockTypefaceCategory.SANS),
    JAKARTA("Jakarta", PlusJakartaSansFontFamily, R.font.plus_jakarta_sans, ClockTypefaceCategory.SANS),
    OSWALD("Oswald", OswaldFontFamily, R.font.oswald, ClockTypefaceCategory.SANS),
    SYNE("Syne", SyneFontFamily, R.font.syne, ClockTypefaceCategory.SANS),
    PLAYFAIR("Playfair", PlayfairFontFamily, R.font.playfair_display, ClockTypefaceCategory.SERIF),
    CORMORANT("Cormorant", CormorantFontFamily, R.font.cormorant_garamond, ClockTypefaceCategory.SERIF),
    CINZEL("Cinzel", CinzelFontFamily, R.font.cinzel, ClockTypefaceCategory.SERIF),
    INSTRUMENT_SERIF("Instrument", InstrumentSerifFontFamily, R.font.instrument_serif, ClockTypefaceCategory.SERIF),
    BODONI_MODA("Bodoni", BodoniModaFontFamily, R.font.bodoni_moda, ClockTypefaceCategory.SERIF),
    FREDOKA("Rounded", FredokaFontFamily, R.font.fredoka, ClockTypefaceCategory.ROUNDED),
    RIGHTEOUS("Righteous", RighteousFontFamily, R.font.righteous, ClockTypefaceCategory.ROUNDED),
    JETBRAINS_MONO("Mono", JetBrainsMonoFontFamily, R.font.jetbrains_mono, ClockTypefaceCategory.MONO),
    SPACE_GROTESK("Space", SpaceGroteskFontFamily, R.font.space_grotesk, ClockTypefaceCategory.MONO),
    ORBITRON("Orbitron", OrbitronFontFamily, R.font.orbitron, ClockTypefaceCategory.MONO),
    SYNE_MONO("Syne Mono", SyneMonoFontFamily, R.font.syne_mono, ClockTypefaceCategory.MONO),
    SILKSCREEN("Pixel LED", SilkscreenFontFamily, R.font.silkscreen, ClockTypefaceCategory.MONO),
    MAJOR_MONO("Major", MajorMonoFontFamily, R.font.major_mono_display, ClockTypefaceCategory.MONO),
    SHANTELL_SANS("Script", ShantellSansFontFamily, R.font.shantell_sans, ClockTypefaceCategory.SCRIPT),
    CAVEAT("Caveat", CaveatFontFamily, R.font.caveat, ClockTypefaceCategory.SCRIPT)
}

enum class HyperOsClockFinish(val label: String) {
    SOLID("Solid"),
    TRANSLUCENT("Translucent"),
    BLEND("Gradient"),
    OUTLINE("Outline"),
    STRIPED("Striped"),
    BLUR_GLASS("Liquid Glass")
}

data class ClockWidgetTelemetry(
    val weatherText: String = "21° CLEAR",
    val batteryPercent: Int = 86,
    val isWeatherAvailable: Boolean = true
)

data class ClockDesignConfig(
    val face: HyperOsClockFace = HyperOsClockFace.POSTER_SPLIT,
    val font: HyperOsClockFont = HyperOsClockFace.POSTER_SPLIT.recommendedFont,
    val finish: HyperOsClockFinish = HyperOsClockFinish.BLEND,
    val weightLevel: Int = 850,
    val scale: Float = 1.0f,
    val widthScale: Float = 1.0f,
    val heightScale: Float = 1.0f,
    val baselineScale: Float = 1.0f,
    val baselineWidthScale: Float = 1.0f,
    val baselineHeightScale: Float = 1.0f,
    val opacity: Float = 0.96f,
    val placementY: Float = 0.14f,
    val offsetXFraction: Float = 0f,
    val alignment: ClockHorizontalAlign = ClockHorizontalAlign.CENTER,
    val useLiveTime: Boolean = false,
    val is24HourFormat: Boolean = true,
    val showDateAndWeather: Boolean = true,
    val widgetsEnabled: Boolean = true,
    val showDateWidget: Boolean = true,
    val showWeatherWidget: Boolean = true,
    val showBatteryWidget: Boolean = false,
    val showDecorativeWidget: Boolean = true,
    val signatureMode: ClockSignatureMode = ClockSignatureMode.DATE_WEATHER,
    val signatureText: String = "",
    val aspectAdaptiveEnabled: Boolean = true,
    val customPrimaryArgb: Long? = null,
    val customSecondaryArgb: Long? = null,
    val depthOverlapEnabled: Boolean = true,
    val liquidGlass: LiquidGlassConfig = LiquidGlassConfig()
) {
    val hasCustomProportions: Boolean
        get() = kotlin.math.abs(widthScale - baselineWidthScale) > 0.02f ||
            kotlin.math.abs(heightScale - baselineHeightScale) > 0.02f ||
            kotlin.math.abs(scale - baselineScale) > 0.02f

    fun toJsonString(): String = JSONObject().apply {
        put("face", face.name)
        put("font", font.name)
        put("finish", finish.name)
        put("weightLevel", weightLevel)
        put("scale", scale.toDouble())
        put("widthScale", widthScale.toDouble())
        put("heightScale", heightScale.toDouble())
        put("opacity", opacity.toDouble())
        put("placementY", placementY.toDouble())
        put("offsetXFraction", offsetXFraction.toDouble())
        put("alignment", alignment.name)
        put("useLiveTime", useLiveTime)
        put("is24HourFormat", is24HourFormat)
        put("showDateAndWeather", showDateAndWeather)
        put("widgetsEnabled", widgetsEnabled)
        put("showDateWidget", showDateWidget)
        put("showWeatherWidget", showWeatherWidget)
        put("showBatteryWidget", showBatteryWidget)
        put("showDecorativeWidget", showDecorativeWidget)
        put("signatureMode", signatureMode.name)
        put("signatureText", signatureText)
        put("aspectAdaptiveEnabled", aspectAdaptiveEnabled)
        customPrimaryArgb?.let { put("customPrimaryArgb", it) }
        customSecondaryArgb?.let { put("customSecondaryArgb", it) }
        put("depthOverlapEnabled", depthOverlapEnabled)
        put("liquidGlass", liquidGlass.toJsonObject())
    }.toString()

    companion object {
        fun fromJsonString(raw: String?): ClockDesignConfig? {
            if (raw.isNullOrBlank()) return null
            return runCatching {
                val obj = JSONObject(raw)
                val face = runCatching { HyperOsClockFace.valueOf(obj.optString("face")) }
                    .getOrDefault(HyperOsClockFace.POSTER_SPLIT)
                val font = runCatching { HyperOsClockFont.valueOf(obj.optString("font")) }
                    .getOrDefault(face.recommendedFont)
                val finish = runCatching { HyperOsClockFinish.valueOf(obj.optString("finish")) }
                    .getOrDefault(HyperOsClockFinish.BLEND)
                val align = runCatching { ClockHorizontalAlign.valueOf(obj.optString("alignment")) }
                    .getOrDefault(ClockHorizontalAlign.CENTER)
                val showDate = obj.optBoolean("showDateAndWeather", true)
                val sigMode = runCatching { ClockSignatureMode.valueOf(obj.optString("signatureMode")) }
                    .getOrDefault(if (showDate) ClockSignatureMode.DATE_WEATHER else ClockSignatureMode.PURE_TIME)
                val primaryArg = if (obj.has("customPrimaryArgb")) obj.optLong("customPrimaryArgb") else null
                val secondaryArg = if (obj.has("customSecondaryArgb")) obj.optLong("customSecondaryArgb") else null
                val glassConfig = LiquidGlassConfig.fromJsonObject(obj.optJSONObject("liquidGlass"))

                ClockDesignConfig(
                    face = face,
                    font = font,
                    finish = finish,
                    weightLevel = obj.optInt("weightLevel", 850).coerceIn(100, 900),
                    scale = obj.optDouble("scale", 1.0).toFloat().coerceIn(ClockSpatialMath.MIN_SCALE, ClockSpatialMath.MAX_SCALE),
                    widthScale = obj.optDouble("widthScale", 1.0).toFloat().coerceIn(ClockSpatialMath.MIN_WIDTH_SCALE, ClockSpatialMath.MAX_WIDTH_SCALE),
                    heightScale = obj.optDouble("heightScale", 1.0).toFloat().coerceIn(ClockSpatialMath.MIN_HEIGHT_SCALE, ClockSpatialMath.MAX_HEIGHT_SCALE),
                    opacity = obj.optDouble("opacity", 0.96).toFloat().coerceIn(0.20f, 1.0f),
                    placementY = obj.optDouble("placementY", 0.14).toFloat().coerceIn(0.02f, 0.92f),
                    offsetXFraction = obj.optDouble("offsetXFraction", 0.0).toFloat().coerceIn(-0.48f, 0.48f),
                    alignment = align,
                    useLiveTime = obj.optBoolean("useLiveTime", false),
                    is24HourFormat = obj.optBoolean("is24HourFormat", true),
                    showDateAndWeather = showDate,
                    widgetsEnabled = obj.optBoolean("widgetsEnabled", true),
                    showDateWidget = obj.optBoolean("showDateWidget", showDate),
                    showWeatherWidget = obj.optBoolean("showWeatherWidget", showDate),
                    showBatteryWidget = obj.optBoolean("showBatteryWidget", false),
                    showDecorativeWidget = obj.optBoolean("showDecorativeWidget", true),
                    signatureMode = sigMode,
                    signatureText = obj.optString("signatureText", "").take(36),
                    aspectAdaptiveEnabled = obj.optBoolean("aspectAdaptiveEnabled", true),
                    customPrimaryArgb = primaryArg,
                    customSecondaryArgb = secondaryArg,
                    depthOverlapEnabled = obj.optBoolean("depthOverlapEnabled", true),
                    liquidGlass = glassConfig
                )
            }.getOrNull()
        }
    }
}

internal data class ClockTimeParts(
    val hours: String,
    val minutes: String,
    val seconds: String,
    val dateLabel: String,
    val amPmLabel: String,
    val hourInt: Int,
    val minuteInt: Int,
    val secondInt: Int
)

@OptIn(ExperimentalTextApi::class)
@Composable
fun rememberClockFontFamily(font: HyperOsClockFont, weightLevel: Int): FontFamily {
    val clampedWeight = weightLevel.coerceIn(100, 900)
    val quantizedWeight = ((clampedWeight + 12) / 25 * 25).coerceIn(100, 900)
    return remember(font, quantizedWeight) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching {
                FontFamily(
                    Font(
                        resId = font.resId,
                        weight = FontWeight(quantizedWeight),
                        variationSettings = FontVariation.Settings(
                            FontVariation.weight(quantizedWeight)
                        )
                    )
                )
            }.getOrDefault(font.fontFamily)
        } else {
            font.fontFamily
        }
    }
}

@Composable
fun HyperOsLockClock(
    face: HyperOsClockFace,
    font: HyperOsClockFont,
    finish: HyperOsClockFinish,
    primaryColor: Color,
    secondaryColor: Color,
    weightLevel: Int = 850,
    scaleFactor: Float = 1.0f,
    widthScale: Float = 1.0f,
    heightScale: Float = 1.0f,
    opacity: Float = 0.96f,
    alignment: ClockHorizontalAlign = ClockHorizontalAlign.CENTER,
    useLiveTime: Boolean = false,
    is24HourFormat: Boolean = true,
    showDateAndWeather: Boolean = true,
    widgetsEnabled: Boolean = true,
    showDateWidget: Boolean = true,
    showWeatherWidget: Boolean = true,
    showBatteryWidget: Boolean = false,
    showDecorativeWidget: Boolean = true,
    telemetry: ClockWidgetTelemetry = ClockWidgetTelemetry(),
    signatureMode: ClockSignatureMode = ClockSignatureMode.DATE_WEATHER,
    signatureText: String = "",
    liquidGlassConfig: LiquidGlassConfig = LiquidGlassConfig(),
    backdropBitmap: Bitmap? = null,
    viewportSize: IntSize = IntSize.Zero,
    clockBoxRect: ClockBoxRect? = null,
    zoomScale: Float = 1.0f,
    panOffset: Offset = Offset.Zero,
    dimFraction: Float = 0.0f,
    lookFilter: CanvasLookFilter = CanvasLookFilter.ORIGINAL,
    onMeasuredUnscaledSize: ((IntSize) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(useLiveTime) {
        if (useLiveTime) {
            while (true) {
                nowMillis = System.currentTimeMillis()
                delay(1000L)
            }
        }
    }

    val parts = remember(useLiveTime, is24HourFormat, nowMillis) {
        if (!useLiveTime) {
            ClockTimeParts(
                hours = if (is24HourFormat) "09" else "9",
                minutes = "41",
                seconds = "28",
                dateLabel = "FRI, NOV 15",
                amPmLabel = if (is24HourFormat) "" else "AM",
                hourInt = 9,
                minuteInt = 41,
                secondInt = 28
            )
        } else {
            val d = Date(nowMillis)
            val hhPattern = if (is24HourFormat) "HH" else "hh"
            val hh = SimpleDateFormat(hhPattern, Locale.getDefault()).format(d)
            val mm = SimpleDateFormat("mm", Locale.getDefault()).format(d)
            val ss = SimpleDateFormat("ss", Locale.getDefault()).format(d)
            val amPm = if (is24HourFormat) "" else SimpleDateFormat("a", Locale.getDefault()).format(d).uppercase(Locale.getDefault())
            val dateStr = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(d).uppercase(Locale.getDefault())
            val rawHour24 = SimpleDateFormat("H", Locale.getDefault()).format(d).toIntOrNull() ?: 9
            ClockTimeParts(
                hours = hh,
                minutes = mm,
                seconds = ss,
                dateLabel = dateStr,
                amPmLabel = amPm,
                hourInt = rawHour24,
                minuteInt = mm.toIntOrNull() ?: 41,
                secondInt = ss.toIntOrNull() ?: 0
            )
        }
    }

    val colAlignment = when (alignment) {
        ClockHorizontalAlign.START -> Alignment.Start
        ClockHorizontalAlign.CENTER -> Alignment.CenterHorizontally
        ClockHorizontalAlign.END -> Alignment.End
    }
    val textAlign = when (alignment) {
        ClockHorizontalAlign.START -> TextAlign.Start
        ClockHorizontalAlign.CENTER -> TextAlign.Center
        ClockHorizontalAlign.END -> TextAlign.End
    }

    val uiStyle = LocalUiStyleMode.current
    val reducedMotion = LocalReducedMotionFlag.current
    val customTokens = LocalThemeCustomization.current
    val scale = scaleFactor.coerceIn(ClockSpatialMath.MIN_SCALE, ClockSpatialMath.MAX_SCALE)
    val safeWidth = widthScale.coerceIn(ClockSpatialMath.MIN_WIDTH_SCALE, ClockSpatialMath.MAX_WIDTH_SCALE)
    val safeHeight = heightScale.coerceIn(ClockSpatialMath.MIN_HEIGHT_SCALE, ClockSpatialMath.MAX_HEIGHT_SCALE)
    val safeOpacity = opacity.coerceIn(0.20f, 1.0f)
    val animatedOpacity by animateFloatAsState(
        targetValue = safeOpacity,
        animationSpec = AppMotion.floatEffectsSpec(uiStyle, reducedMotion, customTokens.m3MotionSpeed),
        label = "clock_opacity_anim"
    )
    val safeWeight = FontWeight(weightLevel.coerceIn(100, 900))
    val activeFamily = rememberClockFontFamily(font, weightLevel)

    val morphPulse = remember { androidx.compose.animation.core.Animatable(1f) }
    LaunchedEffect(face, finish, reducedMotion) {
        if (!reducedMotion) {
            morphPulse.snapTo(if (finish == HyperOsClockFinish.BLUR_GLASS || uiStyle == UiStyleMode.FROSTED_GLASS_OS) 0.90f else 0.95f)
            morphPulse.animateTo(
                targetValue = 1f,
                animationSpec = AppMotion.glassMorphSpring(uiStyle, reducedMotion = false, speedMultiplier = customTokens.m3MotionSpeed)
            )
        } else {
            morphPulse.snapTo(1f)
        }
    }

    @Composable
    fun ClockFaceColumn(
        renderFace: HyperOsClockFace,
        renderFinish: HyperOsClockFinish,
        facePrimary: Color,
        faceSecondary: Color,
        maskMode: Boolean
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalAlignment = colAlignment
        ) {
            RenderHyperOsClockFace(
                face = renderFace,
                parts = parts,
                fontFamily = activeFamily,
                fontWeight = safeWeight,
                weightLevel = weightLevel,
                finish = renderFinish,
                primaryColor = facePrimary,
                secondaryColor = faceSecondary,
                scale = scale,
                widthScale = safeWidth,
                opacity = if (maskMode) 1f else animatedOpacity,
                showDateAndWeather = showDateAndWeather,
                widgetsEnabled = widgetsEnabled,
                showDateWidget = showDateWidget,
                showWeatherWidget = showWeatherWidget,
                showBatteryWidget = showBatteryWidget,
                showDecorativeWidget = showDecorativeWidget,
                telemetry = telemetry,
                signatureMode = signatureMode,
                signatureText = signatureText,
                colAlignment = colAlignment,
                textAlign = textAlign,
                renderAsGlyphMask = maskMode
            )
        }
    }

    Box(
        modifier = modifier
            .wrapContentSize()
            .onSizeChanged { measured ->
                onMeasuredUnscaledSize?.invoke(measured)
            }
            .graphicsLayer {
                scaleX = safeWidth * morphPulse.value
                scaleY = safeHeight * morphPulse.value
                transformOrigin = TransformOrigin(0.5f, 0f)
                alpha = if (finish == HyperOsClockFinish.BLUR_GLASS) 1f else animatedOpacity
            },
        contentAlignment = Alignment.TopCenter
    ) {
        if (finish == HyperOsClockFinish.BLUR_GLASS) {
            val captureKey = remember(
                face,
                font,
                weightLevel,
                scale,
                alignment,
                is24HourFormat,
                showDateAndWeather,
                widgetsEnabled,
                showDateWidget,
                showWeatherWidget,
                showBatteryWidget,
                showDecorativeWidget,
                telemetry,
                signatureMode,
                signatureText,
                parts.hours,
                parts.minutes,
                parts.seconds,
                parts.dateLabel
            ) {
                listOf(
                    face,
                    font,
                    weightLevel,
                    scale,
                    alignment,
                    is24HourFormat,
                    showDateAndWeather,
                    widgetsEnabled,
                    showDateWidget,
                    showWeatherWidget,
                    showBatteryWidget,
                    showDecorativeWidget,
                    telemetry.weatherText,
                    telemetry.batteryPercent,
                    signatureMode,
                    signatureText,
                    parts.hours,
                    parts.minutes,
                    parts.seconds
                )
            }
            // Keyed per face so switching faces inside Liquid Glass never retains previous face bounds or stretches offscreen masks
            key(face) {
                LiquidGlassClockLayer(
                    liquidGlassConfig = liquidGlassConfig,
                    backdropBitmap = backdropBitmap,
                    viewportSize = viewportSize,
                    clockBoxRect = clockBoxRect,
                    zoomScale = zoomScale,
                    panOffset = panOffset,
                    dimFraction = dimFraction,
                    lookFilter = lookFilter,
                    fallbackPrimary = primaryColor,
                    fallbackSecondary = secondaryColor,
                    scaleXAspect = safeWidth,
                    scaleYAspect = safeHeight,
                    opacity = animatedOpacity,
                    captureKey = captureKey
                ) {
                    ClockFaceColumn(
                        renderFace = face,
                        renderFinish = HyperOsClockFinish.SOLID,
                        facePrimary = Color.White,
                        faceSecondary = Color.White.copy(alpha = 0.96f),
                        maskMode = true
                    )
                }
            }
        } else {
            AnimatedContent(
                targetState = face,
                transitionSpec = {
                    if (reducedMotion) {
                        fadeIn(tween(100)) togetherWith fadeOut(tween(80))
                    } else {
                        AppMotion.screenEnter(uiStyle, reducedMotion = false) togetherWith
                            AppMotion.screenExit(uiStyle, reducedMotion = false)
                    }
                },
                label = "clock_face_switch"
            ) { targetFace ->
                ClockFaceColumn(
                    renderFace = targetFace,
                    renderFinish = finish,
                    facePrimary = primaryColor,
                    faceSecondary = secondaryColor,
                    maskMode = false
                )
            }
        }
    }
}
