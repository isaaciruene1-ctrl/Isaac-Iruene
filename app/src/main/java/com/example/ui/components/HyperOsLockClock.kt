package com.example.ui.components

import android.os.Build
import androidx.annotation.FontRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.engine.depth.ClockHorizontalAlign
import com.example.ui.theme.BigShouldersFontFamily
import com.example.ui.theme.CormorantFontFamily
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.OswaldFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlayfairFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.ShantellSansFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.SyneFontFamily
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class HyperOsClockCategory(val label: String) {
    MAGAZINE("Magazine"),
    DOODLE("Doodle"),
    ANALOG_PIXEL("Analog & Pixel"),
    GEOMETRIC_SEAL("Geometric & Seal")
}

enum class HyperOsClockFace(
    val label: String,
    val subtitle: String,
    val recommendedFont: HyperOsClockFont,
    val category: HyperOsClockCategory
) {
    MAGAZINE_STACKED("Magazine", "Stacked bold editorial", HyperOsClockFont.OSWALD, HyperOsClockCategory.MAGAZINE),
    POSTER_SPLIT("Poster Bold", "Giant condensed HyperOS numerals", HyperOsClockFont.BIG_SHOULDERS, HyperOsClockCategory.MAGAZINE),
    CLASSIC_HORIZON("Editorial", "Wide classic lock display", HyperOsClockFont.OUTFIT, HyperOsClockCategory.MAGAZINE),
    ATELIER_SERIF("Atelier", "Refined high-fashion serif", HyperOsClockFont.PLAYFAIR, HyperOsClockCategory.MAGAZINE),
    DOODLE_PLAY("Doodle Play", "Playful HyperOS curves & stars", HyperOsClockFont.FREDOKA, HyperOsClockCategory.DOODLE),
    CUTOUT_POP("Pop Sticker", "Hand-drawn playful sticker badges", HyperOsClockFont.SHANTELL_SANS, HyperOsClockCategory.DOODLE),
    ANALOG_MINIMAL("Analog Dial", "Minimal sweeping hands dial", HyperOsClockFont.SPACE_GROTESK, HyperOsClockCategory.ANALOG_PIXEL),
    PIXEL_DUAL_DIAL("Pixel Dual", "Two-tone Pixel display & seconds arc", HyperOsClockFont.SPACE_GROTESK, HyperOsClockCategory.ANALOG_PIXEL),
    KINETIC_MONO("Mono", "Tabular precision & live seconds", HyperOsClockFont.JETBRAINS_MONO, HyperOsClockCategory.ANALOG_PIXEL),
    RHOMBUS_PRISM("Rhombus", "HyperOS geometric diamond frame", HyperOsClockFont.ORBITRON, HyperOsClockCategory.GEOMETRIC_SEAL),
    CALLIGRAPHY_SEAL("Oriental Seal", "Vertical calligraphic chop stamp", HyperOsClockFont.CORMORANT, HyperOsClockCategory.GEOMETRIC_SEAL)
}

enum class HyperOsClockFont(
    val label: String,
    val family: FontFamily,
    @param:FontRes val resId: Int
) {
    OSWALD("Oswald", OswaldFontFamily, R.font.oswald),
    BIG_SHOULDERS("Poster", BigShouldersFontFamily, R.font.big_shoulders_display),
    OUTFIT("Outfit", OutfitFontFamily, R.font.outfit),
    FREDOKA("Doodle", FredokaFontFamily, R.font.fredoka),
    SHANTELL_SANS("Marker", ShantellSansFontFamily, R.font.shantell_sans),
    PLAYFAIR("Playfair", PlayfairFontFamily, R.font.playfair_display),
    CORMORANT("Cormorant", CormorantFontFamily, R.font.cormorant_garamond),
    ORBITRON("Geometric", OrbitronFontFamily, R.font.orbitron),
    SYNE("Syne", SyneFontFamily, R.font.syne),
    SPACE_GROTESK("Space", SpaceGroteskFontFamily, R.font.space_grotesk),
    JAKARTA("Jakarta", PlusJakartaSansFontFamily, R.font.plus_jakarta_sans),
    JETBRAINS_MONO("Mono", JetBrainsMonoFontFamily, R.font.jetbrains_mono)
}

enum class HyperOsClockFinish(val label: String) {
    SOLID("Solid"),
    TRANSLUCENT("Translucent"),
    BLEND("Blend"),
    OUTLINE("Outline"),
    BLUR_GLASS("Blur Glass")
}

data class ClockDesignConfig(
    val face: HyperOsClockFace = HyperOsClockFace.MAGAZINE_STACKED,
    val font: HyperOsClockFont = HyperOsClockFace.MAGAZINE_STACKED.recommendedFont,
    val finish: HyperOsClockFinish = HyperOsClockFinish.BLEND,
    val weightLevel: Int = 700,
    val scale: Float = 1.0f,
    val widthScale: Float = 1.0f,
    val heightScale: Float = 1.0f,
    val opacity: Float = 0.94f,
    val placementY: Float = 0.14f,
    val offsetXFraction: Float = 0f,
    val alignment: ClockHorizontalAlign = ClockHorizontalAlign.CENTER,
    val useLiveTime: Boolean = false,
    val showDateAndWeather: Boolean = true
)

internal data class ClockTimeParts(
    val hours: String,
    val minutes: String,
    val seconds: String,
    val dateLabel: String,
    val hourInt: Int,
    val minuteInt: Int,
    val secondInt: Int
)

@OptIn(ExperimentalTextApi::class)
@Composable
fun rememberClockFontFamily(font: HyperOsClockFont, weightLevel: Int): FontFamily {
    val clampedWeight = weightLevel.coerceIn(100, 900)
    // Snap variation updates in steps of 25 so recomposition & font cache stay buttery smooth
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
            }.getOrDefault(font.family)
        } else {
            font.family
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
    weightLevel: Int = 700,
    scaleFactor: Float = 1.0f,
    widthScale: Float = 1.0f,
    heightScale: Float = 1.0f,
    opacity: Float = 0.96f,
    alignment: ClockHorizontalAlign = ClockHorizontalAlign.CENTER,
    useLiveTime: Boolean = false,
    showDateAndWeather: Boolean = true,
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

    val parts = remember(useLiveTime, nowMillis) {
        if (!useLiveTime) {
            ClockTimeParts(
                hours = "09",
                minutes = "41",
                seconds = "28",
                dateLabel = "FRI, NOV 15",
                hourInt = 9,
                minuteInt = 41,
                secondInt = 28
            )
        } else {
            val d = Date(nowMillis)
            val hh = SimpleDateFormat("HH", Locale.getDefault()).format(d)
            val mm = SimpleDateFormat("mm", Locale.getDefault()).format(d)
            val ss = SimpleDateFormat("ss", Locale.getDefault()).format(d)
            val dateStr = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(d).uppercase(Locale.getDefault())
            ClockTimeParts(
                hours = hh,
                minutes = mm,
                seconds = ss,
                dateLabel = dateStr,
                hourInt = hh.toIntOrNull() ?: 9,
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

    val scale = scaleFactor.coerceIn(0.70f, 1.45f)
    val safeWidth = widthScale.coerceIn(0.72f, 1.35f)
    val safeHeight = heightScale.coerceIn(0.70f, 1.55f)
    val safeOpacity = opacity.coerceIn(0.20f, 1.0f)
    val safeWeight = FontWeight(weightLevel.coerceIn(100, 900))
    val activeFamily = rememberClockFontFamily(font, weightLevel)

    val transformOrigin = when (alignment) {
        ClockHorizontalAlign.START -> TransformOrigin(0f, 0.25f)
        ClockHorizontalAlign.CENTER -> TransformOrigin(0.5f, 0.25f)
        ClockHorizontalAlign.END -> TransformOrigin(1f, 0.25f)
    }

    val clockContent: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = colAlignment
        ) {
            RenderHyperOsClockFace(
                face = face,
                parts = parts,
                fontFamily = activeFamily,
                fontWeight = safeWeight,
                weightLevel = weightLevel,
                finish = finish,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                scale = scale,
                opacity = safeOpacity,
                showDateAndWeather = showDateAndWeather,
                colAlignment = colAlignment,
                textAlign = textAlign
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = safeWidth
                scaleY = safeHeight
                this.transformOrigin = transformOrigin
                alpha = safeOpacity
            },
        contentAlignment = when (alignment) {
            ClockHorizontalAlign.START -> Alignment.TopStart
            ClockHorizontalAlign.CENTER -> Alignment.TopCenter
            ClockHorizontalAlign.END -> Alignment.TopEnd
        }
    ) {
        if (finish == HyperOsClockFinish.BLUR_GLASS) {
            val glassShape = RoundedCornerShape(32.dp)
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(glassShape)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.20f),
                                    secondaryColor.copy(alpha = 0.16f),
                                    Color.White.copy(alpha = 0.10f)
                                )
                            )
                        )
                        .let { mod ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                mod.blur(18.dp)
                            } else {
                                mod
                            }
                        }
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.30f),
                            shape = glassShape
                        )
                )
                Box(modifier = Modifier.padding(vertical = 10.dp)) {
                    clockContent()
                }
            }
        } else {
            clockContent()
        }
    }
}
