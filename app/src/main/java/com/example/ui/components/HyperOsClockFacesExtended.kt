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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BigShouldersFontFamily
import com.example.ui.theme.CormorantFontFamily
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.OswaldFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.SyneFontFamily
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Shared polished metadata & signature strip supporting all [ClockSignatureMode] variants
 * while preserving razor-sharp alpha silhouettes when [renderAsGlyphMask] is true for Liquid Glass.
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
    isCapsuleStyled: Boolean = false
) {
    if (!showDateAndWeather || signatureMode == ClockSignatureMode.PURE_TIME) return

    val effectiveText = when (signatureMode) {
        ClockSignatureMode.DATE_WEATHER -> signatureText.ifBlank { "${parts.dateLabel}  •  21° CLEAR" }
        ClockSignatureMode.WEEKDAY_SECONDS -> signatureText.ifBlank { "${parts.dateLabel}  •  :${parts.seconds}" }
        ClockSignatureMode.MINIMAL_DATE -> signatureText.ifBlank { parts.dateLabel }
        ClockSignatureMode.EDITORIAL_TAG -> signatureText.ifBlank {
            if (parts.amPmLabel.isNotEmpty()) "${parts.dateLabel}  •  ${parts.amPmLabel} EDITION"
            else "${parts.dateLabel}  •  STUDIO EDITION"
        }
        ClockSignatureMode.PURE_TIME -> return
    }

    val safePrimary = if (renderAsGlyphMask) Color.White else primaryColor
    val safeSecondary = if (renderAsGlyphMask) Color.White else secondaryColor

    if (isCapsuleStyled) {
        Surface(
            color = if (renderAsGlyphMask) Color.Transparent else primaryColor.copy(alpha = 0.16f),
            shape = RoundedCornerShape(50),
            border = BorderStroke(
                width = 1.4.dp,
                color = if (renderAsGlyphMask) Color.White else primaryColor.copy(alpha = 0.42f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                when (signatureMode) {
                    ClockSignatureMode.DATE_WEATHER -> {
                        Icon(
                            imageVector = Icons.Rounded.WbSunny,
                            contentDescription = null,
                            tint = safeSecondary,
                            modifier = Modifier.size((13f * scale).dp)
                        )
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
                Text(
                    text = effectiveText,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = (11.5f * scale).sp,
                    letterSpacing = 1.4.sp,
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
            if (signatureMode == ClockSignatureMode.DATE_WEATHER && signatureText.isBlank()) {
                Icon(
                    imageVector = Icons.Rounded.WbSunny,
                    contentDescription = null,
                    tint = safeSecondary,
                    modifier = Modifier.size((13.5f * scale).dp)
                )
            }
            Text(
                text = effectiveText,
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = (12f * scale).sp,
                letterSpacing = 1.8.sp,
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

    when (face) {
        // 1. Canyon Monument: giant softly rounded stacked numerals for deep canyon/portrait tuck
        HyperOsClockFace.CANYON_MONUMENT -> {
            Column(
                horizontalAlignment = colAlignment,
                verticalArrangement = Arrangement.spacedBy((-8 * scale).dp)
            ) {
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
                    fontFamily = OutfitFontFamily
                )
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

        // 2. Luxe Vogue: tightly kerned ultra-tall condensed serif numerals (0941, no colon)
        HyperOsClockFace.LUXE_VOGUE -> {
            Column(
                horizontalAlignment = colAlignment,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
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
                    fontFamily = CormorantFontFamily
                )
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

        // 3. Spire Monolith: edge-to-edge tall architectural capsule numerals (0941 side-by-side)
        HyperOsClockFace.SPIRE_MONOLITH -> {
            Column(
                horizontalAlignment = colAlignment,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
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
                    fontFamily = SpaceGroteskFontFamily
                )
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

        // 4. Aqua Mist Tall: airy slender stacked glass numerals tailored for ocean & Liquid Glass bevels
        HyperOsClockFace.AQUA_MIST_TALL -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy((-4f * scale).dp)
            ) {
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
                    fontFamily = PlusJakartaSansFontFamily
                )
                StyledClockText(
                    text = parts.hours,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight((weightLevel - 150).coerceIn(200, 700)),
                    weightLevel = (weightLevel - 150).coerceIn(200, 700),
                    fontSize = (114f * scale).sp,
                    lineHeight = (98f * scale).sp,
                    letterSpacing = 1.sp,
                    finish = finish,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .width((44f * scale).dp)
                        .height(1.8.dp)
                        .background(maskAccent.copy(alpha = if (renderAsGlyphMask) 1f else 0.72f))
                )
                StyledClockText(
                    text = parts.minutes,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight((weightLevel - 150).coerceIn(200, 700)),
                    weightLevel = (weightLevel - 150).coerceIn(200, 700),
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

        // 5. Horizon Silk: broad horizontal luminous rounded-bold numerals (wide-aspect flagship companion)
        HyperOsClockFace.HORIZON_SILK -> {
            Column(
                horizontalAlignment = colAlignment,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
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
                    fontFamily = OutfitFontFamily
                )
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

        // 6. Meadow Block: heavy compact asymmetric stacked geometric numerals + side signature accent
        HyperOsClockFace.MEADOW_BLOCK -> {
            Column(
                horizontalAlignment = colAlignment,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
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
                    fontFamily = PlusJakartaSansFontFamily
                )
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
                    if (showDateAndWeather && signatureMode != ClockSignatureMode.PURE_TIME) {
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

        // 7. Swiss Grid Split: architectural hairline grid with asymmetric hour/minute quadrants
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
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height((68f * scale).dp)
                            .background(maskAccent.copy(alpha = if (renderAsGlyphMask) 1f else 0.78f))
                    )
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
                if (showDateAndWeather && signatureMode != ClockSignatureMode.PURE_TIME) {
                    Box(
                        modifier = Modifier
                            .width((210f * scale).dp)
                            .height(1.5.dp)
                            .background(maskWhite.copy(alpha = if (renderAsGlyphMask) 1f else 0.65f))
                    )
                    ClockSignatureMetadataStrip(
                        parts = parts,
                        signatureMode = signatureMode,
                        signatureText = signatureText,
                        showDateAndWeather = true,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        scale = scale,
                        shadow = shadow,
                        renderAsGlyphMask = renderAsGlyphMask,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                }
            }
        }

        // 8. Orbital Ring Hybrid: concentric analog tick ring orbiting crisp digital core
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
                    fontFamily = OrbitronFontFamily,
                    isCapsuleStyled = true
                )
            }
        }

        // 9. Chrono Telemetry: horizontal performance readout + live 60-second bar + capsule
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
                // 60-second horizontal telemetry gauge
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
                    fontFamily = JetBrainsMonoFontFamily
                )
            }
        }

        // 10. Glass Pill Trio: modular rounded capsules separating hours, minutes, and metadata
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
                        color = if (renderAsGlyphMask) Color.Transparent else primaryColor.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(26.dp),
                        border = BorderStroke(2.dp, if (renderAsGlyphMask) Color.White else primaryColor.copy(alpha = 0.65f))
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
                        color = if (renderAsGlyphMask) Color.Transparent else secondaryColor.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(26.dp),
                        border = BorderStroke(2.dp, if (renderAsGlyphMask) Color.White else secondaryColor.copy(alpha = 0.72f))
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
                    fontFamily = SyneFontFamily,
                    isCapsuleStyled = true
                )
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
        else -> Unit
    }
}
