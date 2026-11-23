package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.OswaldFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.RighteousFontFamily
import com.example.ui.theme.SilkscreenFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.SyneFontFamily
import com.example.ui.theme.SyneMonoFontFamily
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Shared polished metadata, live telemetry & signature strip supporting independent slots:
 * Date, Live Weather, Battery telemetry, and custom Signature NEVER overwrite each other.
 * Zero elevation shadows on translucent capsules prevents white rectangular slabs in Light Mode.
 */
@Composable
internal fun ClockSignatureMetadataStrip(
    parts: ClockTimeParts,
    signatureMode: ClockSignatureMode,
    signatureText: String,
    showDateAndWeather: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    scale: Float,
    shadow: Shadow?,
    renderAsGlyphMask: Boolean,
    fontFamily: FontFamily = PlusJakartaSansFontFamily,
    isCapsuleStyled: Boolean = false,
    widgetsEnabled: Boolean = true,
    showDateWidget: Boolean = true,
    showWeatherWidget: Boolean = true,
    showBatteryWidget: Boolean = false,
    showDecorativeWidget: Boolean = true,
    telemetry: ClockWidgetTelemetry = ClockWidgetTelemetry()
) {
    val cleanSignature = signatureText.trim()
    val showTelemetryRow = showDateAndWeather && signatureMode != ClockSignatureMode.PURE_TIME
    if (!showTelemetryRow && cleanSignature.isEmpty()) return

    val segments = buildList {
        if (showTelemetryRow) {
            if (showDateWidget) {
                add(parts.dateLabel)
            }
            if (widgetsEnabled && showWeatherWidget && signatureMode == ClockSignatureMode.DATE_WEATHER) {
                add(telemetry.weatherText)
            }
            if (signatureMode == ClockSignatureMode.WEEKDAY_SECONDS) {
                add(":${parts.seconds}")
            }
            if (widgetsEnabled && showBatteryWidget) {
                add("${telemetry.batteryPercent}%")
            }
            if (signatureMode == ClockSignatureMode.EDITORIAL_TAG) {
                add(if (parts.amPmLabel.isNotEmpty()) "${parts.amPmLabel} EDITION" else "STUDIO EDITION")
            }
        }
    }
    val telemetryLine = segments.joinToString("  •  ")

    val safePrimary = if (renderAsGlyphMask) Color.White else primaryColor
    val safeSecondary = if (renderAsGlyphMask) Color.White else secondaryColor

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (telemetryLine.isNotEmpty()) {
            if (isCapsuleStyled) {
                Surface(
                    color = if (renderAsGlyphMask) Color.Transparent else primaryColor.copy(alpha = 0.14f),
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        width = 1.25.dp,
                        color = if (renderAsGlyphMask) Color.White else primaryColor.copy(alpha = 0.42f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (showDecorativeWidget) {
                            when (signatureMode) {
                                ClockSignatureMode.DATE_WEATHER -> {
                                    if (widgetsEnabled && showWeatherWidget) {
                                        Icon(
                                            imageVector = Icons.Rounded.WbSunny,
                                            contentDescription = null,
                                            tint = safeSecondary,
                                            modifier = Modifier.size((13f * scale).dp)
                                        )
                                    }
                                }
                                ClockSignatureMode.WEEKDAY_SECONDS -> {
                                    Icon(
                                        imageVector = Icons.Rounded.Schedule,
                                        contentDescription = null,
                                        tint = safeSecondary,
                                        modifier = Modifier.size((13f * scale).dp)
                                    )
                                }
                                ClockSignatureMode.EDITORIAL_TAG -> {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = safeSecondary,
                                        modifier = Modifier.size((13f * scale).dp)
                                    )
                                }
                                else -> Unit
                            }
                        }
                        if (widgetsEnabled && showBatteryWidget && signatureMode == ClockSignatureMode.MINIMAL_DATE) {
                            Icon(
                                imageVector = Icons.Rounded.BatteryChargingFull,
                                contentDescription = null,
                                tint = safeSecondary,
                                modifier = Modifier.size((12.5f * scale).dp)
                            )
                        }
                        Text(
                            text = telemetryLine,
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = (11.5f * scale).sp,
                            letterSpacing = 1.3.sp,
                            color = safePrimary.copy(alpha = if (renderAsGlyphMask) 1f else 0.94f),
                            style = TextStyle(shadow = shadow),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (showDecorativeWidget && widgetsEnabled && showWeatherWidget && signatureMode == ClockSignatureMode.DATE_WEATHER) {
                        Icon(
                            imageVector = Icons.Rounded.WbSunny,
                            contentDescription = null,
                            tint = safeSecondary,
                            modifier = Modifier.size((13.5f * scale).dp)
                        )
                    }
                    Text(
                        text = telemetryLine,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = (12f * scale).sp,
                        letterSpacing = 1.6.sp,
                        color = safePrimary.copy(alpha = if (renderAsGlyphMask) 1f else 0.92f),
                        style = TextStyle(shadow = shadow),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (parts.amPmLabel.isNotEmpty() && signatureMode != ClockSignatureMode.EDITORIAL_TAG) {
                        Text(
                            text = parts.amPmLabel,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = (10.5f * scale).sp,
                            color = safeSecondary,
                            style = TextStyle(shadow = shadow)
                        )
                    }
                }
            }
        }

        if (cleanSignature.isNotEmpty() && signatureMode != ClockSignatureMode.PURE_TIME) {
            Surface(
                color = if (renderAsGlyphMask) Color.Transparent else secondaryColor.copy(alpha = 0.14f),
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
                shape = RoundedCornerShape(50),
                border = if (renderAsGlyphMask) {
                    BorderStroke(1.dp, Color.White.copy(alpha = 0.85f))
                } else {
                    BorderStroke(0.8.dp, secondaryColor.copy(alpha = 0.36f))
                }
            ) {
                Text(
                    text = cleanSignature.uppercase(Locale.getDefault()),
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = (10f * scale).sp,
                    letterSpacing = 1.6.sp,
                    color = safeSecondary.copy(alpha = if (renderAsGlyphMask) 1f else 0.96f),
                    style = TextStyle(shadow = shadow),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
internal fun RenderExtendedHyperOsClockFace(
    face: HyperOsClockFace,
    parts: ClockTimeParts,
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    weightLevel: Int,
    finish: HyperOsClockFinish,
    primaryColor: Color,
    secondaryColor: Color,
    scale: Float,
    opacity: Float,
    showDateAndWeather: Boolean,
    widgetsEnabled: Boolean,
    showDateWidget: Boolean,
    showWeatherWidget: Boolean,
    showBatteryWidget: Boolean,
    showDecorativeWidget: Boolean,
    telemetry: ClockWidgetTelemetry,
    signatureMode: ClockSignatureMode,
    signatureText: String,
    colAlignment: Alignment.Horizontal,
    textAlign: TextAlign,
    renderAsGlyphMask: Boolean
) {
    val shadow = if (renderAsGlyphMask) {
        null
    } else {
        Shadow(
            color = Color.Black.copy(alpha = 0.40f * opacity),
            offset = Offset(0f, 6f),
            blurRadius = 16f
        )
    }
    val maskWhite = if (renderAsGlyphMask) Color.White else primaryColor
    val maskAccent = if (renderAsGlyphMask) Color.White else secondaryColor
    val showMeta = showDateAndWeather && signatureMode != ClockSignatureMode.PURE_TIME

    @Composable
    fun SharedStrip(stripFont: FontFamily = PlusJakartaSansFontFamily, capsule: Boolean = false) {
        ClockSignatureMetadataStrip(
            parts = parts,
            signatureMode = signatureMode,
            signatureText = signatureText,
            showDateAndWeather = showDateAndWeather,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            scale = scale,
            shadow = shadow,
            renderAsGlyphMask = renderAsGlyphMask,
            fontFamily = stripFont,
            isCapsuleStyled = capsule,
            widgetsEnabled = widgetsEnabled,
            showDateWidget = showDateWidget,
            showWeatherWidget = showWeatherWidget,
            showBatteryWidget = showBatteryWidget,
            showDecorativeWidget = showDecorativeWidget,
            telemetry = telemetry
        )
    }

    when (face) {
        // 1. Canyon Monument
        HyperOsClockFace.CANYON_MONUMENT -> {
            Column(
                horizontalAlignment = colAlignment,
                verticalArrangement = Arrangement.spacedBy((-8 * scale).dp)
            ) {
                SharedStrip(OutfitFontFamily)
                Spacer(modifier = Modifier.height(2.dp))
                StyledClockText(
                    text = parts.hours,
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    weightLevel = weightLevel,
                    fontSize = (122f * scale).sp,
                    lineHeight = (102f * scale).sp,
                    letterSpacing = (-3).sp,
                    finish = finish,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    textAlign = textAlign
                )
                StyledClockText(
                    text = parts.minutes,
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    weightLevel = weightLevel,
                    fontSize = (122f * scale).sp,
                    lineHeight = (100f * scale).sp,
                    letterSpacing = (-3).sp,
                    finish = finish,
                    primaryColor = secondaryColor,
                    secondaryColor = primaryColor,
                    textAlign = textAlign
                )
            }
        }

        // 2. Luxe Vogue
        HyperOsClockFace.LUXE_VOGUE -> {
            Column(
                horizontalAlignment = colAlignment,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                SharedStrip(CormorantFontFamily)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((-4f * scale).dp)
                ) {
                    StyledClockText(
                        text = parts.hours,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        weightLevel = weightLevel,
                        fontSize = (114f * scale).sp,
                        lineHeight = (104f * scale).sp,
                        letterSpacing = (-4.5).sp,
                        finish = finish,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        textAlign = textAlign
                    )
                    StyledClockText(
                        text = parts.minutes,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        weightLevel = weightLevel,
                        fontSize = (114f * scale).sp,
                        lineHeight = (104f * scale).sp,
                        letterSpacing = (-4.5).sp,
                        finish = finish,
                        primaryColor = secondaryColor,
                        secondaryColor = primaryColor,
                        textAlign = textAlign
                    )
                }
            }
        }

        // 3. Spire Monolith
        HyperOsClockFace.SPIRE_MONOLITH -> {
            Column(
                horizontalAlignment = colAlignment,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                SharedStrip(SpaceGroteskFontFamily)
                StyledClockText(
                    text = "${parts.hours}${parts.minutes}",
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    weightLevel = weightLevel,
                    fontSize = (128f * scale).sp,
                    lineHeight = (116f * scale).sp,
                    letterSpacing = (-3).sp,
                    finish = finish,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    textAlign = textAlign
                )
            }
        }

        // 4. Aqua Mist Tall
        HyperOsClockFace.AQUA_MIST_TALL -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy((-4f * scale).dp)
            ) {
                SharedStrip(PlusJakartaSansFontFamily)
                StyledClockText(
                    text = parts.hours,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight((weightLevel - 140).coerceIn(200, 720)),
                    weightLevel = (weightLevel - 140).coerceIn(200, 720),
                    fontSize = (114f * scale).sp,
                    lineHeight = (98f * scale).sp,
                    letterSpacing = 1.sp,
                    finish = finish,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    textAlign = TextAlign.Center
                )
                if (showDecorativeWidget) {
                    Box(
                        modifier = Modifier
                            .width((44f * scale).dp)
                            .height(1.8.dp)
                            .background(maskAccent.copy(alpha = if (renderAsGlyphMask) 1f else 0.72f))
                    )
                }
                StyledClockText(
                    text = parts.minutes,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight((weightLevel - 140).coerceIn(200, 720)),
                    weightLevel = (weightLevel - 140).coerceIn(200, 720),
                    fontSize = (114f * scale).sp,
                    lineHeight = (98f * scale).sp,
                    letterSpacing = 1.sp,
                    finish = finish,
                    primaryColor = secondaryColor,
                    secondaryColor = primaryColor,
                    textAlign = TextAlign.Center
                )
            }
        }

        // 5. Horizon Silk
        HyperOsClockFace.HORIZON_SILK -> {
            Column(
                horizontalAlignment = colAlignment,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SharedStrip(OutfitFontFamily)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((4f * scale).dp)
                ) {
                    StyledClockText(
                        text = parts.hours,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        weightLevel = weightLevel,
                        fontSize = (102f * scale).sp,
                        lineHeight = (94f * scale).sp,
                        letterSpacing = (-1.5).sp,
                        finish = finish,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        textAlign = textAlign
                    )
                    StyledClockText(
                        text = ":",
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        weightLevel = weightLevel,
                        fontSize = (88f * scale).sp,
                        lineHeight = (88f * scale).sp,
                        letterSpacing = 0.sp,
                        finish = finish,
                        primaryColor = secondaryColor,
                        secondaryColor = primaryColor,
                        textAlign = TextAlign.Center
                    )
                    StyledClockText(
                        text = parts.minutes,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        weightLevel = weightLevel,
                        fontSize = (102f * scale).sp,
                        lineHeight = (94f * scale).sp,
                        letterSpacing = (-1.5).sp,
                        finish = finish,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        textAlign = textAlign
                    )
                }
            }
        }

        // 6. Meadow Block
        HyperOsClockFace.MEADOW_BLOCK -> {
            Column(
                horizontalAlignment = colAlignment,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                SharedStrip(PlusJakartaSansFontFamily)
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy((10f * scale).dp)
                ) {
                    Column(horizontalAlignment = colAlignment) {
                        StyledClockText(
                            text = parts.hours,
                            fontFamily = fontFamily,
                            fontWeight = fontWeight,
                            weightLevel = weightLevel,
                            fontSize = (94f * scale).sp,
                            lineHeight = (80f * scale).sp,
                            letterSpacing = (-2).sp,
                            finish = finish,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            textAlign = textAlign
                        )
                        StyledClockText(
                            text = parts.minutes,
                            fontFamily = fontFamily,
                            fontWeight = fontWeight,
                            weightLevel = weightLevel,
                            fontSize = (94f * scale).sp,
                            lineHeight = (80f * scale).sp,
                            letterSpacing = (-2).sp,
                            finish = finish,
                            primaryColor = secondaryColor,
                            secondaryColor = primaryColor,
                            textAlign = textAlign
                        )
                    }
                    if (showMeta && showDecorativeWidget) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .width(3.dp)
                                .height((54f * scale).dp)
                                .clip(RoundedCornerShape(50))
                                .background(maskAccent.copy(alpha = if (renderAsGlyphMask) 1f else 0.85f))
                        )
                    }
                }
            }
        }

        // 7. Swiss Grid Split
        HyperOsClockFace.SWISS_GRID_SPLIT -> {
            Column(
                horizontalAlignment = colAlignment,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((10f * scale).dp)
                ) {
                    StyledClockText(
                        text = parts.hours,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        weightLevel = weightLevel,
                        fontSize = (92f * scale).sp,
                        lineHeight = (86f * scale).sp,
                        letterSpacing = (-1).sp,
                        finish = finish,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        textAlign = TextAlign.End
                    )
                    if (showDecorativeWidget) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height((68f * scale).dp)
                                .background(maskAccent.copy(alpha = if (renderAsGlyphMask) 1f else 0.78f))
                        )
                    }
                    StyledClockText(
                        text = parts.minutes,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        weightLevel = weightLevel,
                        fontSize = (92f * scale).sp,
                        lineHeight = (86f * scale).sp,
                        letterSpacing = (-1).sp,
                        finish = finish,
                        primaryColor = secondaryColor,
                        secondaryColor = primaryColor,
                        textAlign = TextAlign.Start
                    )
                }
                if (showMeta) {
                    if (showDecorativeWidget) {
                        Box(
                            modifier = Modifier
                                .width((210f * scale).dp)
                                .height(1.5.dp)
                                .background(maskWhite.copy(alpha = if (renderAsGlyphMask) 1f else 0.65f))
                        )
                    }
                    SharedStrip(JetBrainsMonoFontFamily)
                }
            }
        }

        // 8. Orbital Ring Hybrid
        HyperOsClockFace.ORBITAL_RING_HYBRID -> {
            val dialSize = (186f * scale).dp
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier.size(dialSize),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val radius = size.minDimension * 0.46f
                        val center = Offset(size.width * 0.5f, size.height * 0.5f)
                        drawCircle(
                            color = maskWhite.copy(alpha = if (renderAsGlyphMask) 1f else 0.42f),
                            radius = radius,
                            center = center,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        val sweep = ((parts.secondInt / 60f) * 360f).coerceAtLeast(18f)
                        drawArc(
                            color = maskAccent,
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2f, radius * 2f),
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                        for (i in 0 until 12) {
                            val angleRad = (i * 30.0 - 90.0) * PI / 180.0
                            val rIn = radius * 0.85f
                            val rOut = radius * 0.93f
                            drawLine(
                                color = maskWhite.copy(alpha = if (renderAsGlyphMask) 1f else 0.72f),
                                start = Offset(center.x + (rIn * cos(angleRad)).toFloat(), center.y + (rIn * sin(angleRad)).toFloat()),
                                end = Offset(center.x + (rOut * cos(angleRad)).toFloat(), center.y + (rOut * sin(angleRad)).toFloat()),
                                strokeWidth = 2.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        StyledClockText(
                            text = "${parts.hours}:${parts.minutes}",
                            fontFamily = fontFamily,
                            fontWeight = fontWeight,
                            weightLevel = weightLevel,
                            fontSize = (46f * scale).sp,
                            lineHeight = (46f * scale).sp,
                            letterSpacing = 0.5.sp,
                            finish = finish,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (parts.amPmLabel.isNotEmpty()) "${parts.seconds}s ${parts.amPmLabel}" else "${parts.seconds}s",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = (11f * scale).sp,
                            color = maskAccent,
                            style = TextStyle(shadow = shadow)
                        )
                    }
                }
                SharedStrip(OrbitronFontFamily, capsule = true)
            }
        }

        // 9. Chrono Telemetry
        HyperOsClockFace.CHRONO_TELEMETRY -> {
            Column(
                horizontalAlignment = colAlignment,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy((8f * scale).dp)
                ) {
                    StyledClockText(
                        text = "${parts.hours}:${parts.minutes}",
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        weightLevel = weightLevel,
                        fontSize = (88f * scale).sp,
                        lineHeight = (82f * scale).sp,
                        letterSpacing = 0.5.sp,
                        finish = finish,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        textAlign = textAlign
                    )
                    Text(
                        text = parts.seconds,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = (24f * scale).sp,
                        color = maskAccent,
                        modifier = Modifier.padding(bottom = 12.dp),
                        style = TextStyle(shadow = shadow)
                    )
                }
                if (showDecorativeWidget) {
                    val progress = (parts.secondInt / 60f).coerceIn(0.06f, 1f)
                    Box(
                        modifier = Modifier
                            .width((232f * scale).dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(50))
                            .border(1.dp, maskWhite.copy(alpha = if (renderAsGlyphMask) 1f else 0.45f), RoundedCornerShape(50))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(5.dp)
                                .clip(RoundedCornerShape(50))
                                .background(maskAccent)
                        )
                    }
                }
                SharedStrip(JetBrainsMonoFontFamily)
            }
        }

        // 10. Glass Pill Trio
        HyperOsClockFace.GLASS_PILL_TRIO -> {
            Column(
                horizontalAlignment = colAlignment,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((10f * scale).dp)
                ) {
                    Surface(
                        color = if (renderAsGlyphMask) Color.Transparent else primaryColor.copy(alpha = 0.16f),
                        shadowElevation = 0.dp,
                        tonalElevation = 0.dp,
                        shape = RoundedCornerShape(26.dp),
                        border = BorderStroke(1.8.dp, if (renderAsGlyphMask) Color.White else primaryColor.copy(alpha = 0.65f))
                    ) {
                        Box(modifier = Modifier.padding(horizontal = (16f * scale).dp, vertical = (4f * scale).dp)) {
                            StyledClockText(
                                text = parts.hours,
                                fontFamily = fontFamily,
                                fontWeight = fontWeight,
                                weightLevel = weightLevel,
                                fontSize = (74f * scale).sp,
                                lineHeight = (74f * scale).sp,
                                letterSpacing = 0.sp,
                                finish = finish,
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Surface(
                        color = if (renderAsGlyphMask) Color.Transparent else secondaryColor.copy(alpha = 0.20f),
                        shadowElevation = 0.dp,
                        tonalElevation = 0.dp,
                        shape = RoundedCornerShape(26.dp),
                        border = BorderStroke(1.8.dp, if (renderAsGlyphMask) Color.White else secondaryColor.copy(alpha = 0.72f))
                    ) {
                        Box(modifier = Modifier.padding(horizontal = (16f * scale).dp, vertical = (4f * scale).dp)) {
                            StyledClockText(
                                text = parts.minutes,
                                fontFamily = fontFamily,
                                fontWeight = fontWeight,
                                weightLevel = weightLevel,
                                fontSize = (74f * scale).sp,
                                lineHeight = (74f * scale).sp,
                                letterSpacing = 0.sp,
                                finish = finish,
                                primaryColor = secondaryColor,
                                secondaryColor = primaryColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                SharedStrip(SyneFontFamily, capsule = true)
            }
        }

        // --- 10 BRAND NEW INNOVATIVE FACES ---

        // 11. Tall Poster 1340 (Inspired by towering ultra-condensed poster numerals)
        HyperOsClockFace.TALL_POSTER_1340 -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                SharedStrip(BebasNeueFontFamily, capsule = true)
                StyledClockText(
                    text = "${parts.hours}${parts.minutes}",
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    weightLevel = weightLevel,
                    fontSize = (138f * scale).sp,
                    lineHeight = (122f * scale).sp,
                    letterSpacing = (-3.5).sp,
                    finish = finish,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    textAlign = TextAlign.Center
                )
            }
        }

        // 12. Roman Monument
        HyperOsClockFace.ROMAN_MONUMENT -> {
            Column(
                horizontalAlignment = colAlignment,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((10f * scale).dp)
                ) {
                    StyledClockText(
                        text = parts.hours,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        weightLevel = weightLevel,
                        fontSize = (96f * scale).sp,
                        lineHeight = (88f * scale).sp,
                        letterSpacing = 1.sp,
                        finish = finish,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        textAlign = textAlign
                    )
                    if (showDecorativeWidget) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(1.5.dp)
                                    .height((18f * scale).dp)
                                    .background(maskAccent.copy(alpha = 0.7f))
                            )
                            Box(
                                modifier = Modifier
                                    .size((7f * scale).dp)
                                    .rotate(45f)
                                    .background(maskAccent)
                            )
                            Box(
                                modifier = Modifier
                                    .width(1.5.dp)
                                    .height((18f * scale).dp)
                                    .background(maskAccent.copy(alpha = 0.7f))
                            )
                        }
                    }
                    StyledClockText(
                        text = parts.minutes,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        weightLevel = weightLevel,
                        fontSize = (96f * scale).sp,
                        lineHeight = (88f * scale).sp,
                        letterSpacing = 1.sp,
                        finish = finish,
                        primaryColor = secondaryColor,
                        secondaryColor = primaryColor,
                        textAlign = textAlign
                    )
                }
                SharedStrip(CinzelFontFamily)
            }
        }

        // 13. Editorial Duo
        HyperOsClockFace.EDITORIAL_DUO -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((12f * scale).dp)
            ) {
                if (showDecorativeWidget) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height((136f * scale).dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.verticalGradient(listOf(maskWhite, maskAccent))
                            )
                    )
                }
                Column(horizontalAlignment = Alignment.Start) {
                    SharedStrip(BodoniModaFontFamily)
                    StyledClockText(
                        text = parts.hours,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        weightLevel = weightLevel,
                        fontSize = (94f * scale).sp,
                        lineHeight = (80f * scale).sp,
                        letterSpacing = (-2).sp,
                        finish = finish,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        textAlign = TextAlign.Start
                    )
                    StyledClockText(
                        text = parts.minutes,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        weightLevel = weightLevel,
                        fontSize = (94f * scale).sp,
                        lineHeight = (80f * scale).sp,
                        letterSpacing = (-2).sp,
                        finish = finish,
                        primaryColor = secondaryColor,
                        secondaryColor = primaryColor,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }

        // 14. Glass Overlap
        HyperOsClockFace.NEON_OVERLAP -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.alpha(if (renderAsGlyphMask) 0.72f else 0.34f).offset(x = (-18f * scale).dp, y = (-12f * scale).dp)) {
                        StyledClockText(
                            text = parts.hours,
                            fontFamily = fontFamily,
                            fontWeight = fontWeight,
                            weightLevel = weightLevel,
                            fontSize = (118f * scale).sp,
                            lineHeight = (108f * scale).sp,
                            letterSpacing = (-3).sp,
                            finish = if (renderAsGlyphMask) HyperOsClockFinish.SOLID else HyperOsClockFinish.OUTLINE,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            textAlign = TextAlign.Center
                        )
                    }
                    Box(modifier = Modifier.offset(x = (16f * scale).dp, y = (22f * scale).dp)) {
                        StyledClockText(
                            text = parts.minutes,
                            fontFamily = fontFamily,
                            fontWeight = fontWeight,
                            weightLevel = weightLevel,
                            fontSize = (106f * scale).sp,
                            lineHeight = (96f * scale).sp,
                            letterSpacing = (-2).sp,
                            finish = finish,
                            primaryColor = secondaryColor,
                            secondaryColor = primaryColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height((20f * scale).dp))
                SharedStrip(ArchivoBlackFontFamily, capsule = true)
            }
        }

        // 15. Matrix LED
        HyperOsClockFace.RETRO_PIXEL_MATRIX -> {
            Column(
                horizontalAlignment = colAlignment,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = if (renderAsGlyphMask) Color.Transparent else Color.Black.copy(alpha = 0.24f),
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.6.dp, maskAccent.copy(alpha = if (renderAsGlyphMask) 1f else 0.75f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = (16f * scale).dp, vertical = (8f * scale).dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (showDecorativeWidget) {
                            Box(
                                modifier = Modifier
                                    .size((8f * scale).dp)
                                    .clip(CircleShape)
                                    .background(maskAccent)
                            )
                        }
                        StyledClockText(
                            text = "${parts.hours}:${parts.minutes}",
                            fontFamily = fontFamily,
                            fontWeight = fontWeight,
                            weightLevel = weightLevel,
                            fontSize = (64f * scale).sp,
                            lineHeight = (64f * scale).sp,
                            letterSpacing = 1.sp,
                            finish = finish,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            textAlign = textAlign
                        )
                    }
                }
                SharedStrip(SilkscreenFontFamily)
            }
        }

        // 16. Solar Horizon Arc Gauge
        HyperOsClockFace.SOLAR_ARC_GAUGE -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (showDecorativeWidget) {
                    val dayProgress = (((parts.hourInt * 60 + parts.minuteInt) / 1440f).coerceIn(0.08f, 0.92f))
                    Canvas(
                        modifier = Modifier.size(width = (210f * scale).dp, height = (48f * scale).dp)
                    ) {
                        val arcRect = Size(size.width * 0.90f, size.height * 1.65f)
                        val left = (size.width - arcRect.width) * 0.5f
                        drawArc(
                            color = maskWhite.copy(alpha = if (renderAsGlyphMask) 0.5f else 0.35f),
                            startAngle = 185f,
                            sweepAngle = 170f,
                            useCenter = false,
                            topLeft = Offset(left, 6.dp.toPx()),
                            size = arcRect,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = maskAccent,
                            startAngle = 185f,
                            sweepAngle = 170f * dayProgress,
                            useCenter = false,
                            topLeft = Offset(left, 6.dp.toPx()),
                            size = arcRect,
                            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                        val theta = (185f + 170f * dayProgress) * PI / 180.0
                        val rx = arcRect.width * 0.5f
                        val ry = arcRect.height * 0.5f
                        val sunX = left + rx + (rx * cos(theta)).toFloat()
                        val sunY = 6.dp.toPx() + ry + (ry * sin(theta)).toFloat()
                        drawCircle(color = maskAccent, radius = 5.dp.toPx(), center = Offset(sunX, sunY))
                    }
                }
                StyledClockText(
                    text = "${parts.hours}:${parts.minutes}",
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    weightLevel = weightLevel,
                    fontSize = (94f * scale).sp,
                    lineHeight = (88f * scale).sp,
                    letterSpacing = (-1).sp,
                    finish = finish,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    textAlign = TextAlign.Center
                )
                SharedStrip(InstrumentSerifFontFamily)
            }
        }

        // 17. Compass Dial Chrono
        HyperOsClockFace.COMPASS_CHRONO -> {
            val dialSize = (192f * scale).dp
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.size(dialSize), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val r = size.minDimension * 0.45f
                        val c = Offset(size.width * 0.5f, size.height * 0.5f)
                        for (deg in 0 until 360 step 10) {
                            val rad = (deg - 90.0) * PI / 180.0
                            val major = deg % 90 == 0
                            val inner = if (major) r * 0.80f else if (deg % 30 == 0) r * 0.86f else r * 0.90f
                            val outer = r * 0.96f
                            drawLine(
                                color = if (major) maskAccent else maskWhite.copy(alpha = if (renderAsGlyphMask) 0.9f else 0.55f),
                                start = Offset(c.x + (inner * cos(rad)).toFloat(), c.y + (inner * sin(rad)).toFloat()),
                                end = Offset(c.x + (outer * cos(rad)).toFloat(), c.y + (outer * sin(rad)).toFloat()),
                                strokeWidth = if (major) 3.dp.toPx() else 1.4.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "N",
                            fontFamily = SyneMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = (10f * scale).sp,
                            color = maskAccent
                        )
                        StyledClockText(
                            text = "${parts.hours}:${parts.minutes}",
                            fontFamily = fontFamily,
                            fontWeight = fontWeight,
                            weightLevel = weightLevel,
                            fontSize = (42f * scale).sp,
                            lineHeight = (44f * scale).sp,
                            letterSpacing = 0.sp,
                            finish = finish,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                SharedStrip(SyneMonoFontFamily, capsule = true)
            }
        }

        // 18. Brutalist Slab
        HyperOsClockFace.BRUTALIST_SLAB -> {
            Column(
                horizontalAlignment = colAlignment,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((6f * scale).dp)
                ) {
                    Surface(
                        color = if (renderAsGlyphMask) Color.Transparent else primaryColor.copy(alpha = 0.22f),
                        shadowElevation = 0.dp,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(2.2.dp, maskWhite)
                    ) {
                        Box(modifier = Modifier.padding(horizontal = (12f * scale).dp, vertical = (4f * scale).dp)) {
                            StyledClockText(
                                text = parts.hours,
                                fontFamily = fontFamily,
                                fontWeight = fontWeight,
                                weightLevel = weightLevel,
                                fontSize = (78f * scale).sp,
                                lineHeight = (74f * scale).sp,
                                letterSpacing = (-2).sp,
                                finish = finish,
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Surface(
                        color = if (renderAsGlyphMask) Color.Transparent else secondaryColor.copy(alpha = 0.22f),
                        shadowElevation = 0.dp,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(2.2.dp, maskAccent)
                    ) {
                        Box(modifier = Modifier.padding(horizontal = (12f * scale).dp, vertical = (4f * scale).dp)) {
                            StyledClockText(
                                text = parts.minutes,
                                fontFamily = fontFamily,
                                fontWeight = fontWeight,
                                weightLevel = weightLevel,
                                fontSize = (78f * scale).sp,
                                lineHeight = (74f * scale).sp,
                                letterSpacing = (-2).sp,
                                finish = finish,
                                primaryColor = secondaryColor,
                                secondaryColor = primaryColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                SharedStrip(ArchivoBlackFontFamily, capsule = true)
            }
        }

        // 19. Kinetic Step Cascade
        HyperOsClockFace.KINETIC_STEP -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(modifier = Modifier.offset(x = (-22f * scale).dp)) {
                    StyledClockText(
                        text = parts.hours,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        weightLevel = weightLevel,
                        fontSize = (96f * scale).sp,
                        lineHeight = (86f * scale).sp,
                        letterSpacing = (-2).sp,
                        finish = finish,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        textAlign = TextAlign.Start
                    )
                }
                if (showDecorativeWidget) {
                    Box(
                        modifier = Modifier
                            .width((92f * scale).dp)
                            .height(3.dp)
                            .rotate(-14f)
                            .clip(RoundedCornerShape(50))
                            .background(maskAccent)
                    )
                }
                Box(modifier = Modifier.offset(x = (22f * scale).dp)) {
                    StyledClockText(
                        text = parts.minutes,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        weightLevel = weightLevel,
                        fontSize = (96f * scale).sp,
                        lineHeight = (86f * scale).sp,
                        letterSpacing = (-2).sp,
                        finish = finish,
                        primaryColor = secondaryColor,
                        secondaryColor = primaryColor,
                        textAlign = TextAlign.End
                    )
                }
                SharedStrip(RighteousFontFamily)
            }
        }

        // 20. Handwritten Studio Script
        HyperOsClockFace.HANDWRITTEN_STAMP -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                StyledClockText(
                    text = "${parts.hours}:${parts.minutes}",
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    weightLevel = weightLevel,
                    fontSize = (102f * scale).sp,
                    lineHeight = (92f * scale).sp,
                    letterSpacing = 1.sp,
                    finish = finish,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    textAlign = TextAlign.Center
                )
                if (showDecorativeWidget) {
                    Canvas(
                        modifier = Modifier.size(width = (176f * scale).dp, height = (14f * scale).dp)
                    ) {
                        val brushPath = Path().apply {
                            moveTo(0f, size.height * 0.72f)
                            cubicTo(
                                size.width * 0.30f, size.height * 0.10f,
                                size.width * 0.65f, size.height * 0.95f,
                                size.width, size.height * 0.36f
                            )
                        }
                        drawPath(
                            path = brushPath,
                            color = maskAccent,
                            style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
                SharedStrip(CaveatFontFamily, capsule = true)
            }
        }

        else -> Unit
    }
}

@Composable
internal fun RenderExtendedClockFaceThumbnail(
    face: HyperOsClockFace,
    accentColor: Color
) {
    when (face) {
        HyperOsClockFace.CANYON_MONUMENT -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("02", fontFamily = OutfitFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, lineHeight = 14.sp, color = Color.White)
                Text("36", fontFamily = OutfitFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, lineHeight = 14.sp, color = accentColor)
            }
        }
        HyperOsClockFace.LUXE_VOGUE -> {
            Text(
                "0236",
                fontFamily = CormorantFontFamily,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                fontSize = 16.sp,
                letterSpacing = (-1).sp,
                color = Color.White
            )
        }
        HyperOsClockFace.SPIRE_MONOLITH -> {
            Text(
                "0236",
                fontFamily = BigShouldersFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = (-0.5).sp,
                color = Color.White
            )
        }
        HyperOsClockFace.AQUA_MIST_TALL -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("02", fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Light, fontSize = 14.sp, lineHeight = 12.sp, color = Color.White)
                Box(modifier = Modifier.width(14.dp).height(1.dp).background(accentColor))
                Text("36", fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Light, fontSize = 14.sp, lineHeight = 12.sp, color = Color(0xFFCDEEE2))
            }
        }
        HyperOsClockFace.HORIZON_SILK -> {
            Text(
                "02:36",
                fontFamily = FredokaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFFFFF3DF)
            )
        }
        HyperOsClockFace.MEADOW_BLOCK -> {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("02", fontFamily = OswaldFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 11.sp, color = Color.White)
                    Text("36", fontFamily = OswaldFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 11.sp, color = accentColor)
                }
                Box(modifier = Modifier.width(2.dp).height(12.dp).background(accentColor, CircleShape))
            }
        }
        HyperOsClockFace.SWISS_GRID_SPLIT -> {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("09", fontFamily = PlusJakartaSansFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                Box(modifier = Modifier.width(1.5.dp).height(16.dp).background(accentColor))
                Text("41", fontFamily = PlusJakartaSansFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = accentColor)
            }
        }
        HyperOsClockFace.ORBITAL_RING_HYBRID -> {
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(30.dp)) {
                    drawCircle(color = Color.White.copy(alpha = 0.45f), style = Stroke(1.5.dp.toPx()))
                    drawArc(accentColor, -90f, 220f, false, style = Stroke(2.2.dp.toPx(), cap = StrokeCap.Round))
                }
                Text("09:41", fontFamily = OrbitronFontFamily, fontWeight = FontWeight.Bold, fontSize = 7.5.sp, color = Color.White)
            }
        }
        HyperOsClockFace.CHRONO_TELEMETRY -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("09:41", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                Box(modifier = Modifier.width(34.dp).height(2.5.dp).background(accentColor, CircleShape))
            }
        }
        HyperOsClockFace.GLASS_PILL_TRIO -> {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Surface(color = Color.White.copy(alpha = 0.24f), shape = RoundedCornerShape(6.dp)) {
                    Text("09", fontFamily = SyneFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
                Surface(color = accentColor.copy(alpha = 0.40f), shape = RoundedCornerShape(6.dp)) {
                    Text("41", fontFamily = SyneFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
            }
        }
        HyperOsClockFace.TALL_POSTER_1340 -> {
            Text(
                "1340",
                fontFamily = BebasNeueFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 23.sp,
                letterSpacing = (-1).sp,
                color = Color.White
            )
        }
        HyperOsClockFace.ROMAN_MONUMENT -> {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("09", fontFamily = CinzelFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                Box(modifier = Modifier.size(4.dp).rotate(45f).background(accentColor))
                Text("41", fontFamily = CinzelFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = accentColor)
            }
        }
        HyperOsClockFace.EDITORIAL_DUO -> {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.width(2.dp).height(24.dp).background(accentColor, CircleShape))
                Column {
                    Text("09", fontFamily = BodoniModaFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 11.sp, color = Color.White)
                    Text("41", fontFamily = BodoniModaFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 11.sp, color = accentColor)
                }
            }
        }
        HyperOsClockFace.NEON_OVERLAP -> {
            Box(contentAlignment = Alignment.Center) {
                Text("09", fontFamily = ArchivoBlackFontFamily, fontSize = 16.sp, color = Color.White.copy(alpha = 0.45f), modifier = Modifier.offset(x = (-4).dp, y = (-3).dp))
                Text("41", fontFamily = ArchivoBlackFontFamily, fontSize = 16.sp, color = accentColor, modifier = Modifier.offset(x = 4.dp, y = 4.dp))
            }
        }
        HyperOsClockFace.RETRO_PIXEL_MATRIX -> {
            Surface(color = Color.Black.copy(alpha = 0.35f), shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, accentColor)) {
                Text("09:41", fontFamily = SilkscreenFontFamily, fontSize = 9.sp, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
            }
        }
        HyperOsClockFace.SOLAR_ARC_GAUGE -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Canvas(modifier = Modifier.size(width = 30.dp, height = 10.dp)) {
                    drawArc(accentColor, 180f, 180f, false, style = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round))
                }
                Text("09:41", fontFamily = InstrumentSerifFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
            }
        }
        HyperOsClockFace.COMPASS_CHRONO -> {
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(30.dp)) {
                    drawCircle(color = accentColor.copy(alpha = 0.75f), style = Stroke(1.5.dp.toPx()))
                }
                Text("09:41", fontFamily = SyneMonoFontFamily, fontSize = 7.5.sp, color = Color.White)
            }
        }
        HyperOsClockFace.BRUTALIST_SLAB -> {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Surface(color = Color.White.copy(alpha = 0.22f), shape = RoundedCornerShape(3.dp), border = BorderStroke(1.dp, Color.White)) {
                    Text("09", fontFamily = ArchivoBlackFontFamily, fontSize = 10.sp, color = Color.White, modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp))
                }
                Surface(color = accentColor.copy(alpha = 0.35f), shape = RoundedCornerShape(3.dp), border = BorderStroke(1.dp, accentColor)) {
                    Text("41", fontFamily = ArchivoBlackFontFamily, fontSize = 10.sp, color = Color.White, modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp))
                }
            }
        }
        HyperOsClockFace.KINETIC_STEP -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("09", fontFamily = RighteousFontFamily, fontSize = 12.sp, lineHeight = 11.sp, color = Color.White, modifier = Modifier.offset(x = (-5).dp))
                Text("41", fontFamily = RighteousFontFamily, fontSize = 12.sp, lineHeight = 11.sp, color = accentColor, modifier = Modifier.offset(x = 5.dp))
            }
        }
        HyperOsClockFace.HANDWRITTEN_STAMP -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("09:41", fontFamily = CaveatFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                Box(modifier = Modifier.width(24.dp).height(1.5.dp).background(accentColor, CircleShape))
            }
        }
        else -> Unit
    }
}
