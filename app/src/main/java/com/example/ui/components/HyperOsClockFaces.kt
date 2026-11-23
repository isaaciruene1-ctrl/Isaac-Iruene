package com.example.ui.components

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
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val LocalGlyphMaskMode = staticCompositionLocalOf { false }

@Composable
internal fun RenderHyperOsClockFace(
    face: HyperOsClockFace,
    parts: ClockTimeParts,
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    weightLevel: Int,
    finish: HyperOsClockFinish,
    primaryColor: Color,
    secondaryColor: Color,
    scale: Float,
    widthScale: Float,
    opacity: Float,
    showDateAndWeather: Boolean,
    widgetsEnabled: Boolean = true,
    showDateWidget: Boolean = true,
    showWeatherWidget: Boolean = true,
    showBatteryWidget: Boolean = false,
    showDecorativeWidget: Boolean = true,
    telemetry: ClockWidgetTelemetry = ClockWidgetTelemetry(),
    signatureMode: ClockSignatureMode = ClockSignatureMode.DATE_WEATHER,
    signatureText: String,
    colAlignment: Alignment.Horizontal,
    textAlign: TextAlign,
    renderAsGlyphMask: Boolean = false
) {
    CompositionLocalProvider(LocalGlyphMaskMode provides renderAsGlyphMask) {
        RenderHyperOsClockFaceBody(
            face = face,
            parts = parts,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            weightLevel = weightLevel,
            finish = finish,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            scale = scale,
            opacity = opacity,
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
            renderAsGlyphMask = renderAsGlyphMask
        )
    }
}

@Composable
private fun RenderHyperOsClockFaceBody(
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
    val colonGapDp = 4.dp
    val showMeta = showDateAndWeather && signatureMode != ClockSignatureMode.PURE_TIME

    when (face) {
        HyperOsClockFace.CLASSIC_HORIZON -> {
            if (showMeta || signatureText.isNotBlank()) {
                ClockSignatureMetadataStrip(
                    parts = parts,
                    signatureMode = signatureMode,
                    signatureText = signatureText,
                    showDateAndWeather = showMeta,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    scale = scale,
                    shadow = shadow,
                    renderAsGlyphMask = renderAsGlyphMask,
                    fontFamily = OutfitFontFamily,
                    widgetsEnabled = widgetsEnabled,
                    showDateWidget = showDateWidget,
                    showWeatherWidget = showWeatherWidget,
                    showBatteryWidget = showBatteryWidget,
                    showDecorativeWidget = showDecorativeWidget,
                    telemetry = telemetry
                )
                Spacer(modifier = Modifier.height(3.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(colonGapDp)
            ) {
                StyledClockText(
                    text = parts.hours,
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    weightLevel = weightLevel,
                    fontSize = (86f * scale).sp,
                    lineHeight = (86f * scale).sp,
                    letterSpacing = 0.5.sp,
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
                    fontSize = (78f * scale).sp,
                    lineHeight = (78f * scale).sp,
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
                    fontSize = (86f * scale).sp,
                    lineHeight = (86f * scale).sp,
                    letterSpacing = 0.5.sp,
                    finish = finish,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    textAlign = textAlign
                )
            }
        }

        HyperOsClockFace.CLASSIC_UTILITY -> {
            Column(
                horizontalAlignment = colAlignment,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ClockSignatureMetadataStrip(
                    parts = parts,
                    signatureMode = signatureMode,
                    signatureText = "",
                    showDateAndWeather = showMeta,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    scale = scale,
                    shadow = shadow,
                    renderAsGlyphMask = renderAsGlyphMask,
                    fontFamily = PlusJakartaSansFontFamily,
                    isCapsuleStyled = true,
                    widgetsEnabled = widgetsEnabled,
                    showDateWidget = showDateWidget,
                    showWeatherWidget = showWeatherWidget,
                    showBatteryWidget = showBatteryWidget,
                    showDecorativeWidget = showDecorativeWidget,
                    telemetry = telemetry
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(colonGapDp)
                ) {
                    StyledClockText(
                        text = parts.hours,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        weightLevel = weightLevel,
                        fontSize = (82f * scale).sp,
                        lineHeight = (82f * scale).sp,
                        letterSpacing = 0.5.sp,
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
                        fontSize = (74f * scale).sp,
                        lineHeight = (74f * scale).sp,
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
                        fontSize = (82f * scale).sp,
                        lineHeight = (82f * scale).sp,
                        letterSpacing = 0.5.sp,
                        finish = finish,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        textAlign = textAlign
                    )
                }
                val caption = signatureText.ifBlank {
                    if (parts.amPmLabel.isNotEmpty()) "${parts.amPmLabel} • FOCUS READY" else "STUDIO LOCK"
                }
                Text(
                    text = caption,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = (11f * scale).sp,
                    letterSpacing = 1.6.sp,
                    color = secondaryColor.copy(alpha = 0.94f),
                    style = TextStyle(shadow = shadow),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        HyperOsClockFace.MAGAZINE_STACKED -> {
            if (showMeta) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = parts.dateLabel,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = (13f * scale).sp,
                        letterSpacing = 2.4.sp,
                        color = if (renderAsGlyphMask) Color.White else primaryColor.copy(alpha = 0.92f),
                        style = TextStyle(shadow = shadow)
                    )
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(1.5.dp)
                            .background(if (renderAsGlyphMask) Color.White else primaryColor.copy(alpha = 0.72f))
                    )
                    val metaRight = when (signatureMode) {
                        ClockSignatureMode.WEEKDAY_SECONDS -> ":${parts.seconds}"
                        ClockSignatureMode.MINIMAL_DATE -> parts.amPmLabel.ifEmpty { "24H" }
                        else -> buildString {
                            if (widgetsEnabled && showWeatherWidget) append(telemetry.weatherText)
                            if (widgetsEnabled && showBatteryWidget) {
                                if (isNotEmpty()) append(" • ")
                                append("${telemetry.batteryPercent}%")
                            }
                            if (parts.amPmLabel.isNotEmpty()) {
                                if (isNotEmpty()) append(" ")
                                append(parts.amPmLabel)
                            }
                            if (isEmpty()) append("24H")
                        }
                    }
                    Text(
                        text = metaRight,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = (12f * scale).sp,
                        color = if (renderAsGlyphMask) Color.White else primaryColor.copy(alpha = 0.92f),
                        style = TextStyle(shadow = shadow)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
            }
            StyledClockText(
                text = parts.hours,
                fontFamily = fontFamily,
                fontWeight = fontWeight,
                weightLevel = weightLevel,
                fontSize = (106f * scale).sp,
                lineHeight = (92f * scale).sp,
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
                fontSize = (106f * scale).sp,
                lineHeight = (90f * scale).sp,
                letterSpacing = (-2).sp,
                finish = finish,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                textAlign = textAlign
            )
            if (signatureText.isNotBlank()) {
                Text(
                    text = signatureText.uppercase(Locale.getDefault()),
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = (11f * scale).sp,
                    letterSpacing = 2.sp,
                    color = secondaryColor,
                    style = TextStyle(shadow = shadow)
                )
            }
        }

        HyperOsClockFace.POSTER_SPLIT -> {
            if (showMeta || signatureText.isNotBlank()) {
                ClockSignatureMetadataStrip(
                    parts = parts,
                    signatureMode = signatureMode,
                    signatureText = signatureText,
                    showDateAndWeather = showMeta,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    scale = scale,
                    shadow = shadow,
                    renderAsGlyphMask = renderAsGlyphMask,
                    fontFamily = SpaceGroteskFontFamily,
                    widgetsEnabled = widgetsEnabled,
                    showDateWidget = showDateWidget,
                    showWeatherWidget = showWeatherWidget,
                    showBatteryWidget = showBatteryWidget,
                    showDecorativeWidget = showDecorativeWidget,
                    telemetry = telemetry
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((6f * scale).dp + colonGapDp * 0.5f)
            ) {
                StyledClockText(
                    text = parts.hours,
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    weightLevel = weightLevel,
                    fontSize = (124f * scale).sp,
                    lineHeight = (110f * scale).sp,
                    letterSpacing = (-2.5).sp,
                    finish = finish,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    textAlign = textAlign
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy((12f * scale).dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size((8f * scale).dp)
                            .background(secondaryColor.copy(alpha = 0.94f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size((8f * scale).dp)
                            .background(primaryColor.copy(alpha = 0.94f), CircleShape)
                    )
                }
                StyledClockText(
                    text = parts.minutes,
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    weightLevel = weightLevel,
                    fontSize = (124f * scale).sp,
                    lineHeight = (110f * scale).sp,
                    letterSpacing = (-2.5).sp,
                    finish = if (finish == HyperOsClockFinish.SOLID && !renderAsGlyphMask) {
                        HyperOsClockFinish.OUTLINE
                    } else {
                        finish
                    },
                    primaryColor = secondaryColor,
                    secondaryColor = primaryColor,
                    textAlign = textAlign
                )
            }
        }

        HyperOsClockFace.ATELIER_SERIF -> {
            if (showMeta || signatureText.isNotBlank()) {
                val atelierLabel = when (signatureMode) {
                    ClockSignatureMode.WEEKDAY_SECONDS -> "${parts.dateLabel}  :${parts.seconds}"
                    else -> parts.dateLabel.lowercase(Locale.getDefault())
                        .replaceFirstChar { it.titlecase(Locale.getDefault()) }
                }
                if (showMeta && showDateWidget) {
                    Text(
                        text = atelierLabel,
                        fontFamily = CormorantFontFamily,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = (19f * scale).sp,
                        letterSpacing = 1.2.sp,
                        color = if (renderAsGlyphMask) Color.White else primaryColor.copy(alpha = 0.94f),
                        style = TextStyle(shadow = shadow)
                    )
                }
                if (signatureText.isNotBlank()) {
                    Text(
                        text = signatureText,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = (11f * scale).sp,
                        letterSpacing = 1.4.sp,
                        color = if (renderAsGlyphMask) Color.White else secondaryColor.copy(alpha = 0.94f),
                        style = TextStyle(shadow = shadow)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(colonGapDp * 0.5f)
            ) {
                StyledClockText(
                    text = parts.hours,
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    weightLevel = weightLevel,
                    fontSize = (92f * scale).sp,
                    lineHeight = (88f * scale).sp,
                    letterSpacing = (-1).sp,
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
                    fontSize = (82f * scale).sp,
                    lineHeight = (80f * scale).sp,
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
                    fontSize = (92f * scale).sp,
                    lineHeight = (88f * scale).sp,
                    letterSpacing = (-1).sp,
                    finish = finish,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    textAlign = textAlign
                )
            }
            if (showMeta) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .width(46.dp)
                        .height(1.5.dp)
                        .background(if (renderAsGlyphMask) Color.White else secondaryColor.copy(alpha = 0.85f))
                )
            }
        }

        HyperOsClockFace.DOODLE_PLAY -> {
            if (showMeta) {
                Surface(
                    color = if (renderAsGlyphMask) Color.Transparent else secondaryColor.copy(alpha = 0.22f),
                    shadowElevation = 0.dp,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .rotate(-3f)
                        .border(1.6.dp, if (renderAsGlyphMask) Color.White else primaryColor.copy(alpha = 0.55f), RoundedCornerShape(50))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (showDecorativeWidget) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = if (renderAsGlyphMask) Color.White else secondaryColor,
                                modifier = Modifier.size((13f * scale).dp)
                            )
                        }
                        Text(
                            text = buildString {
                                if (showDateWidget) append(parts.dateLabel)
                                if (signatureMode == ClockSignatureMode.WEEKDAY_SECONDS) {
                                    if (isNotEmpty()) append(" • ")
                                    append(":${parts.seconds}")
                                } else if (widgetsEnabled && showWeatherWidget && signatureMode == ClockSignatureMode.DATE_WEATHER) {
                                    if (isNotEmpty()) append(" • ")
                                    append(telemetry.weatherText)
                                }
                            }.ifBlank { parts.dateLabel },
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = (12f * scale).sp,
                            color = if (renderAsGlyphMask) Color.White else primaryColor,
                            style = TextStyle(shadow = shadow)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
            if (signatureText.isNotBlank()) {
                Text(
                    text = signatureText,
                    fontFamily = FredokaFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = (11f * scale).sp,
                    color = if (renderAsGlyphMask) Color.White else secondaryColor,
                    style = TextStyle(shadow = shadow)
                )
            }
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(width = (220f * scale).dp, height = (130f * scale).dp)) {
                    val wave = Path().apply {
                        moveTo(size.width * 0.08f, size.height * 0.88f)
                        cubicTo(
                            size.width * 0.30f, size.height * 0.72f,
                            size.width * 0.65f, size.height * 1.02f,
                            size.width * 0.92f, size.height * 0.84f
                        )
                    }
                    drawPath(
                        path = wave,
                        color = secondaryColor.copy(alpha = 0.75f),
                        style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                    val cx = size.width * 0.90f
                    val cy = size.height * 0.18f
                    val arm = 8.dp.toPx()
                    drawLine(primaryColor.copy(alpha = 0.82f), Offset(cx - arm, cy), Offset(cx + arm, cy), 2.5.dp.toPx(), StrokeCap.Round)
                    drawLine(primaryColor.copy(alpha = 0.82f), Offset(cx, cy - arm), Offset(cx, cy + arm), 2.5.dp.toPx(), StrokeCap.Round)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((4f * scale).dp)
                ) {
                    Box(modifier = Modifier.rotate(-4f)) {
                        StyledClockText(
                            text = parts.hours,
                            fontFamily = fontFamily,
                            fontWeight = fontWeight,
                            weightLevel = weightLevel,
                            fontSize = (92f * scale).sp,
                            lineHeight = (92f * scale).sp,
                            letterSpacing = (-2).sp,
                            finish = finish,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            textAlign = textAlign
                        )
                    }
                    Text(
                        text = ":",
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = (70f * scale).sp,
                        color = secondaryColor,
                        style = TextStyle(shadow = shadow)
                    )
                    Box(modifier = Modifier.rotate(4f)) {
                        StyledClockText(
                            text = parts.minutes,
                            fontFamily = fontFamily,
                            fontWeight = fontWeight,
                            weightLevel = weightLevel,
                            fontSize = (92f * scale).sp,
                            lineHeight = (92f * scale).sp,
                            letterSpacing = (-2).sp,
                            finish = finish,
                            primaryColor = secondaryColor,
                            secondaryColor = primaryColor,
                            textAlign = textAlign
                        )
                    }
                }
            }
        }

        HyperOsClockFace.CUTOUT_POP -> {
            Column(
                horizontalAlignment = colAlignment,
                verticalArrangement = Arrangement.spacedBy((4f * scale).dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy((8f * scale).dp)) {
                    Surface(
                        color = if (renderAsGlyphMask) Color.Transparent else primaryColor.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .rotate(-4f)
                            .border(2.dp, if (renderAsGlyphMask) Color.White else primaryColor.copy(alpha = 0.65f), RoundedCornerShape(24.dp))
                    ) {
                        Box(modifier = Modifier.padding(horizontal = (14f * scale).dp, vertical = (2f * scale).dp)) {
                            StyledClockText(
                                text = parts.hours,
                                fontFamily = fontFamily,
                                fontWeight = fontWeight,
                                weightLevel = weightLevel,
                                fontSize = (76f * scale).sp,
                                lineHeight = (76f * scale).sp,
                                letterSpacing = 0.sp,
                                finish = finish,
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Surface(
                        color = if (renderAsGlyphMask) Color.Transparent else secondaryColor.copy(alpha = 0.28f),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .rotate(4f)
                            .border(2.dp, if (renderAsGlyphMask) Color.White else secondaryColor.copy(alpha = 0.70f), RoundedCornerShape(24.dp))
                    ) {
                        Box(modifier = Modifier.padding(horizontal = (14f * scale).dp, vertical = (2f * scale).dp)) {
                            StyledClockText(
                                text = parts.minutes,
                                fontFamily = fontFamily,
                                fontWeight = fontWeight,
                                weightLevel = weightLevel,
                                fontSize = (76f * scale).sp,
                                lineHeight = (76f * scale).sp,
                                letterSpacing = 0.sp,
                                finish = finish,
                                primaryColor = secondaryColor,
                                secondaryColor = primaryColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                if (showMeta || signatureText.isNotBlank()) {
                    ClockSignatureMetadataStrip(
                        parts = parts,
                        signatureMode = signatureMode,
                        signatureText = signatureText,
                        showDateAndWeather = showMeta,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        scale = scale,
                        shadow = shadow,
                        renderAsGlyphMask = renderAsGlyphMask,
                        fontFamily = ShantellSansFontFamily,
                        widgetsEnabled = widgetsEnabled,
                        showDateWidget = showDateWidget,
                        showWeatherWidget = showWeatherWidget,
                        showBatteryWidget = showBatteryWidget,
                        showDecorativeWidget = showDecorativeWidget,
                        telemetry = telemetry
                    )
                }
            }
        }

        HyperOsClockFace.ANALOG_MINIMAL -> {
            Column(
                horizontalAlignment = colAlignment,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val dialSize = (164f * scale).dp
                val strokeFactor = (weightLevel / 600f).coerceIn(0.6f, 1.6f)
                Canvas(modifier = Modifier.size(dialSize)) {
                    val radius = size.minDimension * 0.46f
                    val center = Offset(size.width * 0.5f, size.height * 0.5f)
                    val dialAlpha = when (finish) {
                        HyperOsClockFinish.TRANSLUCENT -> 0.52f
                        HyperOsClockFinish.BLUR_GLASS -> 0.36f
                        else -> 0.20f
                    }
                    if (!renderAsGlyphMask && finish != HyperOsClockFinish.OUTLINE) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = dialAlpha),
                                    secondaryColor.copy(alpha = dialAlpha * 0.65f)
                                ),
                                center = center,
                                radius = radius
                            ),
                            radius = radius,
                            center = center
                        )
                    }
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.88f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = (2.6f * strokeFactor).dp.toPx())
                    )
                    for (i in 0 until 12) {
                        val angleRad = (i * 30.0 - 90.0) * PI / 180.0
                        val inner = if (i % 3 == 0) radius * 0.78f else radius * 0.86f
                        val outer = radius * 0.93f
                        val sw = if (i % 3 == 0) (3.2f * strokeFactor).dp.toPx() else (1.6f * strokeFactor).dp.toPx()
                        drawLine(
                            color = (if (i % 3 == 0) secondaryColor else primaryColor).copy(alpha = 0.92f),
                            start = Offset(center.x + (inner * cos(angleRad)).toFloat(), center.y + (inner * sin(angleRad)).toFloat()),
                            end = Offset(center.x + (outer * cos(angleRad)).toFloat(), center.y + (outer * sin(angleRad)).toFloat()),
                            strokeWidth = sw,
                            cap = StrokeCap.Round
                        )
                    }
                    val hourAngle = (((parts.hourInt % 12) + parts.minuteInt / 60f) * 30f - 90f) * PI / 180.0
                    drawLine(
                        color = primaryColor,
                        start = center,
                        end = Offset(center.x + (radius * 0.52f * cos(hourAngle)).toFloat(), center.y + (radius * 0.52f * sin(hourAngle)).toFloat()),
                        strokeWidth = (5.5f * strokeFactor).dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    val minAngle = ((parts.minuteInt + parts.secondInt / 60f) * 6f - 90f) * PI / 180.0
                    drawLine(
                        color = primaryColor,
                        start = center,
                        end = Offset(center.x + (radius * 0.74f * cos(minAngle)).toFloat(), center.y + (radius * 0.74f * sin(minAngle)).toFloat()),
                        strokeWidth = (3.6f * strokeFactor).dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    val secAngle = (parts.secondInt * 6f - 90f) * PI / 180.0
                    drawLine(
                        color = secondaryColor,
                        start = center,
                        end = Offset(center.x + (radius * 0.82f * cos(secAngle)).toFloat(), center.y + (radius * 0.82f * sin(secAngle)).toFloat()),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawCircle(color = secondaryColor, radius = 5.dp.toPx(), center = center)
                }
                if (showMeta || signatureText.isNotBlank()) {
                    ClockSignatureMetadataStrip(
                        parts = parts,
                        signatureMode = signatureMode,
                        signatureText = signatureText,
                        showDateAndWeather = showMeta,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        scale = scale,
                        shadow = shadow,
                        renderAsGlyphMask = renderAsGlyphMask,
                        fontFamily = fontFamily,
                        isCapsuleStyled = true,
                        widgetsEnabled = widgetsEnabled,
                        showDateWidget = showDateWidget,
                        showWeatherWidget = showWeatherWidget,
                        showBatteryWidget = showBatteryWidget,
                        showDecorativeWidget = showDecorativeWidget,
                        telemetry = telemetry
                    )
                }
            }
        }

        HyperOsClockFace.PIXEL_DUAL_DIAL -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((14f * scale).dp)
            ) {
                Column(horizontalAlignment = colAlignment) {
                    StyledClockText(
                        text = parts.hours,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        weightLevel = weightLevel,
                        fontSize = (86f * scale).sp,
                        lineHeight = (78f * scale).sp,
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
                        fontSize = (86f * scale).sp,
                        lineHeight = (78f * scale).sp,
                        letterSpacing = (-2).sp,
                        finish = finish,
                        primaryColor = secondaryColor,
                        secondaryColor = primaryColor,
                        textAlign = textAlign
                    )
                }
                if (showMeta) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.size((52f * scale).dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val sweep = (parts.secondInt / 60f) * 360f
                                drawCircle(
                                    color = if (renderAsGlyphMask) Color.White.copy(alpha = 0.45f) else primaryColor.copy(alpha = 0.25f),
                                    style = Stroke(width = 3.5.dp.toPx())
                                )
                                drawArc(
                                    color = if (renderAsGlyphMask) Color.White else secondaryColor,
                                    startAngle = -90f,
                                    sweepAngle = sweep.coerceAtLeast(18f),
                                    useCenter = false,
                                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Text(
                                text = parts.seconds,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = (13f * scale).sp,
                                color = if (renderAsGlyphMask) Color.White else primaryColor,
                                style = TextStyle(shadow = shadow)
                            )
                        }
                        Surface(
                            color = if (renderAsGlyphMask) Color.Transparent else primaryColor.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(50),
                            border = if (renderAsGlyphMask) androidx.compose.foundation.BorderStroke(1.5.dp, Color.White) else null
                        ) {
                            Text(
                                text = signatureText.ifBlank { parts.dateLabel.substringBefore(",") },
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = (11f * scale).sp,
                                color = if (renderAsGlyphMask) Color.White else primaryColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        HyperOsClockFace.KINETIC_MONO -> {
            if (showMeta || signatureText.isNotBlank()) {
                ClockSignatureMetadataStrip(
                    parts = parts,
                    signatureMode = signatureMode,
                    signatureText = signatureText,
                    showDateAndWeather = showMeta,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    scale = scale,
                    shadow = shadow,
                    renderAsGlyphMask = renderAsGlyphMask,
                    fontFamily = JetBrainsMonoFontFamily,
                    isCapsuleStyled = true,
                    widgetsEnabled = widgetsEnabled,
                    showDateWidget = showDateWidget,
                    showWeatherWidget = showWeatherWidget,
                    showBatteryWidget = showBatteryWidget,
                    showDecorativeWidget = showDecorativeWidget,
                    telemetry = telemetry
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
            Row(verticalAlignment = Alignment.Bottom) {
                StyledClockText(
                    text = "${parts.hours}:${parts.minutes}",
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    weightLevel = weightLevel,
                    fontSize = (76f * scale).sp,
                    lineHeight = (76f * scale).sp,
                    letterSpacing = 0.5.sp,
                    finish = finish,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    textAlign = textAlign
                )
                Text(
                    text = ":${parts.seconds}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = (22f * scale).sp,
                    color = secondaryColor.copy(alpha = 0.94f),
                    modifier = Modifier.padding(start = 4.dp, bottom = 12.dp),
                    style = TextStyle(shadow = shadow)
                )
            }
        }

        HyperOsClockFace.RHOMBUS_PRISM -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Canvas(
                    modifier = Modifier.size(
                        width = (232f * scale).dp,
                        height = (132f * scale).dp
                    )
                ) {
                    val diamond = Path().apply {
                        moveTo(size.width * 0.5f, 0f)
                        lineTo(size.width, size.height * 0.5f)
                        lineTo(size.width * 0.5f, size.height)
                        lineTo(0f, size.height * 0.5f)
                        close()
                    }
                    if (!renderAsGlyphMask) {
                        drawPath(
                            path = diamond,
                            brush = Brush.linearGradient(
                                listOf(primaryColor.copy(alpha = 0.16f), secondaryColor.copy(alpha = 0.14f))
                            )
                        )
                    }
                    drawPath(
                        path = diamond,
                        brush = Brush.linearGradient(
                            listOf(
                                if (renderAsGlyphMask) Color.White else primaryColor.copy(alpha = 0.88f),
                                if (renderAsGlyphMask) Color.White else secondaryColor.copy(alpha = 0.88f)
                            )
                        ),
                        style = Stroke(width = 2.4.dp.toPx(), join = StrokeJoin.Miter)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (showMeta) {
                        Text(
                            text = signatureText.ifBlank { parts.dateLabel },
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = (10f * scale).sp,
                            letterSpacing = 2.sp,
                            color = if (renderAsGlyphMask) Color.White else secondaryColor,
                            style = TextStyle(shadow = shadow)
                        )
                    }
                    StyledClockText(
                        text = "${parts.hours}:${parts.minutes}",
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        weightLevel = weightLevel,
                        fontSize = (60f * scale).sp,
                        lineHeight = (60f * scale).sp,
                        letterSpacing = 1.sp,
                        finish = finish,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        HyperOsClockFace.RHOMBUS_DUAL_ROW -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (showMeta || signatureText.isNotBlank()) {
                    ClockSignatureMetadataStrip(
                        parts = parts,
                        signatureMode = signatureMode,
                        signatureText = signatureText,
                        showDateAndWeather = showMeta,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        scale = scale,
                        shadow = shadow,
                        renderAsGlyphMask = renderAsGlyphMask,
                        fontFamily = SyneFontFamily,
                        widgetsEnabled = widgetsEnabled,
                        showDateWidget = showDateWidget,
                        showWeatherWidget = showWeatherWidget,
                        showBatteryWidget = showBatteryWidget,
                        showDecorativeWidget = showDecorativeWidget,
                        telemetry = telemetry
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((10f * scale).dp)
                ) {
                    StyledClockText(
                        text = parts.hours,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        weightLevel = weightLevel,
                        fontSize = (88f * scale).sp,
                        lineHeight = (80f * scale).sp,
                        letterSpacing = (-1).sp,
                        finish = finish,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        textAlign = TextAlign.End
                    )
                    Canvas(modifier = Modifier.size((24f * scale).dp, (56f * scale).dp)) {
                        val cy = size.height * 0.5f
                        val cx = size.width * 0.5f
                        val r = size.width * 0.42f
                        val diamond = Path().apply {
                            moveTo(cx, cy - r)
                            lineTo(cx + r, cy)
                            lineTo(cx, cy + r)
                            lineTo(cx - r, cy)
                            close()
                        }
                        drawPath(diamond, brush = Brush.linearGradient(listOf(primaryColor, secondaryColor)))
                    }
                    StyledClockText(
                        text = parts.minutes,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        weightLevel = weightLevel,
                        fontSize = (88f * scale).sp,
                        lineHeight = (80f * scale).sp,
                        letterSpacing = (-1).sp,
                        finish = finish,
                        primaryColor = secondaryColor,
                        secondaryColor = primaryColor,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }

        HyperOsClockFace.CALLIGRAPHY_SEAL -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(horizontalAlignment = colAlignment) {
                    StyledClockText(
                        text = parts.hours,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        weightLevel = weightLevel,
                        fontSize = (92f * scale).sp,
                        lineHeight = (82f * scale).sp,
                        letterSpacing = (-1).sp,
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
                        fontSize = (92f * scale).sp,
                        lineHeight = (82f * scale).sp,
                        letterSpacing = (-1).sp,
                        finish = finish,
                        primaryColor = secondaryColor,
                        secondaryColor = primaryColor,
                        textAlign = textAlign
                    )
                }
                if (showMeta) {
                    Surface(
                        color = if (renderAsGlyphMask) Color.Transparent else Color(0xFFB33A2E).copy(alpha = 0.90f),
                        shape = RoundedCornerShape(10.dp),
                        tonalElevation = if (renderAsGlyphMask) 0.dp else 2.dp,
                        modifier = Modifier.border(
                            width = 1.8.dp,
                            color = if (renderAsGlyphMask) Color.White else Color.White.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = parts.dateLabel.substringBefore(","),
                                fontFamily = PlayfairFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = (11f * scale).sp,
                                letterSpacing = 1.2.sp,
                                color = Color.White
                            )
                            Box(
                                modifier = Modifier
                                    .width(14.dp)
                                    .height(1.dp)
                                    .background(Color.White.copy(alpha = 0.6f))
                            )
                            Text(
                                text = parts.dateLabel.substringAfter(", ").ifBlank { "15" },
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = (11f * scale).sp,
                                color = Color.White
                            )
                            Text(
                                text = when {
                                    signatureMode == ClockSignatureMode.WEEKDAY_SECONDS -> ":${parts.seconds}"
                                    widgetsEnabled && showWeatherWidget -> telemetry.weatherText.substringBefore(" ")
                                    widgetsEnabled && showBatteryWidget -> "${telemetry.batteryPercent}%"
                                    else -> parts.amPmLabel.ifEmpty { "24H" }
                                },
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = (10f * scale).sp,
                                color = Color.White.copy(alpha = 0.90f)
                            )
                        }
                    }
                }
            }
        }

        else -> {
            RenderExtendedHyperOsClockFace(
                face = face,
                parts = parts,
                fontFamily = fontFamily,
                fontWeight = fontWeight,
                weightLevel = weightLevel,
                finish = finish,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                scale = scale,
                opacity = opacity,
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
                renderAsGlyphMask = renderAsGlyphMask
            )
        }
    }
}

@Composable
internal fun StyledClockText(
    text: String,
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    weightLevel: Int,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    letterSpacing: TextUnit,
    finish: HyperOsClockFinish,
    primaryColor: Color,
    secondaryColor: Color,
    textAlign: TextAlign
) {
    val isMask = LocalGlyphMaskMode.current
    val ambientShadow = if (isMask) {
        null
    } else {
        Shadow(
            color = Color.Black.copy(alpha = 0.42f),
            offset = Offset(0f, 6f),
            blurRadius = 18f
        )
    }

    // Optical weight contour synthesis: guarantees visible weight progression (100..900)
    // across both variable fonts and single-weight bundled .ttf display faces.
    val clampedWeight = weightLevel.coerceIn(100, 900)
    val heavyFraction = ((clampedWeight - 520).coerceAtLeast(0) / 380f).coerceIn(0f, 1f)
    val lightAlphaScale = if (clampedWeight < 380) (0.78f + (clampedWeight - 100) / 280f * 0.22f) else 1f
    val dilationStrokeWidth = if (heavyFraction > 0.02f) {
        (1.0f + heavyFraction * 8.6f) * (fontSize.value / 96f).coerceIn(0.45f, 1.45f)
    } else 0f

    @Composable
    fun LayeredOpticalText(baseStyle: TextStyle, strokeBrush: Brush? = null, strokeColor: Color = primaryColor) {
        Box(contentAlignment = Alignment.Center) {
            if (dilationStrokeWidth > 0.05f && finish != HyperOsClockFinish.OUTLINE) {
                val strokeDraw = Stroke(
                    width = dilationStrokeWidth,
                    join = StrokeJoin.Round,
                    cap = StrokeCap.Round
                )
                val strokeStyle = if (isMask || strokeBrush == null) {
                    TextStyle(
                        fontFamily = baseStyle.fontFamily,
                        fontWeight = baseStyle.fontWeight,
                        fontSize = baseStyle.fontSize,
                        lineHeight = baseStyle.lineHeight,
                        letterSpacing = baseStyle.letterSpacing,
                        color = if (isMask) Color.White else strokeColor,
                        drawStyle = strokeDraw
                    )
                } else {
                    TextStyle(
                        fontFamily = baseStyle.fontFamily,
                        fontWeight = baseStyle.fontWeight,
                        fontSize = baseStyle.fontSize,
                        lineHeight = baseStyle.lineHeight,
                        letterSpacing = baseStyle.letterSpacing,
                        brush = strokeBrush,
                        drawStyle = strokeDraw
                    )
                }
                Text(
                    text = text,
                    textAlign = textAlign,
                    style = strokeStyle
                )
            }
            Text(
                text = text,
                textAlign = textAlign,
                style = baseStyle
            )
        }
    }

    when (finish) {
        HyperOsClockFinish.SOLID -> {
            val fillColor = if (isMask) Color.White else primaryColor.copy(alpha = primaryColor.alpha * lightAlphaScale)
            LayeredOpticalText(
                baseStyle = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    letterSpacing = letterSpacing,
                    color = fillColor,
                    shadow = ambientShadow
                ),
                strokeColor = fillColor
            )
        }

        HyperOsClockFinish.TRANSLUCENT -> {
            val fillColor = if (isMask) Color.White else primaryColor.copy(alpha = 0.56f * lightAlphaScale)
            LayeredOpticalText(
                baseStyle = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    letterSpacing = letterSpacing,
                    color = fillColor,
                    shadow = if (isMask) null else Shadow(
                        color = secondaryColor.copy(alpha = 0.35f),
                        offset = Offset(0f, 3f),
                        blurRadius = 14f
                    )
                ),
                strokeColor = fillColor
            )
        }

        HyperOsClockFinish.BLEND -> {
            val blendBrush = Brush.linearGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.96f * lightAlphaScale),
                    secondaryColor.copy(alpha = 0.90f * lightAlphaScale),
                    primaryColor.copy(alpha = 0.84f * lightAlphaScale)
                )
            )
            LayeredOpticalText(
                baseStyle = if (isMask) {
                    TextStyle(
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        letterSpacing = letterSpacing,
                        color = Color.White,
                        shadow = ambientShadow
                    )
                } else {
                    TextStyle(
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        letterSpacing = letterSpacing,
                        brush = blendBrush,
                        shadow = ambientShadow
                    )
                },
                strokeBrush = blendBrush
            )
        }

        HyperOsClockFinish.OUTLINE -> {
            val strokePx = (1.6f + ((clampedWeight - 100) / 800f) * 9.8f).coerceIn(1.6f, 11.4f)
            val outlineDraw = Stroke(
                width = strokePx,
                join = StrokeJoin.Round,
                cap = StrokeCap.Round
            )
            Text(
                text = text,
                textAlign = textAlign,
                style = if (isMask) {
                    TextStyle(
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        letterSpacing = letterSpacing,
                        color = Color.White,
                        drawStyle = outlineDraw,
                        shadow = ambientShadow
                    )
                } else {
                    TextStyle(
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        letterSpacing = letterSpacing,
                        brush = Brush.verticalGradient(
                            colors = listOf(primaryColor, secondaryColor)
                        ),
                        drawStyle = outlineDraw,
                        shadow = ambientShadow
                    )
                }
            )
        }

        HyperOsClockFinish.STRIPED -> {
            val stripeBrush = Brush.linearGradient(
                colorStops = arrayOf(
                    0.00f to primaryColor,
                    0.38f to primaryColor,
                    0.42f to secondaryColor.copy(alpha = 0.92f),
                    0.78f to secondaryColor.copy(alpha = 0.35f),
                    0.82f to primaryColor,
                    1.00f to primaryColor
                ),
                start = Offset.Zero,
                end = Offset(18f, 18f),
                tileMode = TileMode.Repeated
            )
            LayeredOpticalText(
                baseStyle = if (isMask) {
                    TextStyle(
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        letterSpacing = letterSpacing,
                        color = Color.White,
                        shadow = ambientShadow
                    )
                } else {
                    TextStyle(
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        letterSpacing = letterSpacing,
                        brush = stripeBrush,
                        shadow = ambientShadow
                    )
                },
                strokeBrush = stripeBrush
            )
        }

        HyperOsClockFinish.BLUR_GLASS -> {
            val glassBrush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.96f * lightAlphaScale),
                    primaryColor.copy(alpha = 0.80f * lightAlphaScale),
                    secondaryColor.copy(alpha = 0.72f * lightAlphaScale)
                )
            )
            val glassShadow = if (isMask) null else Shadow(
                color = Color.Black.copy(alpha = 0.34f),
                offset = Offset(0f, 4f),
                blurRadius = 14f
            )
            LayeredOpticalText(
                baseStyle = if (isMask) {
                    TextStyle(
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        letterSpacing = letterSpacing,
                        color = Color.White,
                        shadow = glassShadow
                    )
                } else {
                    TextStyle(
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        letterSpacing = letterSpacing,
                        brush = glassBrush,
                        shadow = glassShadow
                    )
                },
                strokeBrush = glassBrush
            )
        }
    }
}

@Composable
fun HyperOsClockFaceThumbnail(
    face: HyperOsClockFace,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        tonalElevation = if (selected) 4.dp else 1.dp,
        modifier = modifier.size(width = 88.dp, height = 84.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF1B1E28),
                                accentColor.copy(alpha = 0.55f)
                            )
                        ),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (face) {
                    HyperOsClockFace.CLASSIC_HORIZON -> {
                        Text("09:41", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    }
                    HyperOsClockFace.CLASSIC_UTILITY -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("FRI 21°", fontFamily = PlusJakartaSansFontFamily, fontSize = 7.sp, color = Color.White.copy(alpha = 0.8f))
                            Text("09:41", fontFamily = PlusJakartaSansFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        }
                    }
                    HyperOsClockFace.MAGAZINE_STACKED -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("09", fontFamily = OswaldFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 13.sp, color = Color.White)
                            Text("41", fontFamily = OswaldFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 13.sp, color = Color.White)
                        }
                    }
                    HyperOsClockFace.POSTER_SPLIT -> {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("09", fontFamily = BigShouldersFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White)
                            Text(":", fontFamily = BigShouldersFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = accentColor)
                            Text(
                                "41",
                                style = TextStyle(
                                    fontFamily = BigShouldersFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    color = Color.White,
                                    drawStyle = Stroke(width = 2.5f)
                                )
                            )
                        }
                    }
                    HyperOsClockFace.ATELIER_SERIF -> {
                        Text("09:41", fontFamily = PlayfairFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                    HyperOsClockFace.DOODLE_PLAY -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("09", fontFamily = FredokaFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White, modifier = Modifier.rotate(-6f))
                            Text(":", fontFamily = FredokaFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFFFD8C8))
                            Text("41", fontFamily = FredokaFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFE7DEFF), modifier = Modifier.rotate(6f))
                        }
                    }
                    HyperOsClockFace.CUTOUT_POP -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Surface(color = Color.White.copy(alpha = 0.28f), shape = RoundedCornerShape(6.dp), modifier = Modifier.rotate(-5f)) {
                                Text("09", fontFamily = ShantellSansFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                            Surface(color = accentColor.copy(alpha = 0.45f), shape = RoundedCornerShape(6.dp), modifier = Modifier.rotate(5f)) {
                                Text("41", fontFamily = ShantellSansFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                    }
                    HyperOsClockFace.ANALOG_MINIMAL -> {
                        Canvas(modifier = Modifier.size(28.dp)) {
                            drawCircle(color = Color.White, style = Stroke(width = 2.dp.toPx()))
                            drawLine(Color.White, center, Offset(center.x + 5.dp.toPx(), center.y - 3.dp.toPx()), 2.dp.toPx(), StrokeCap.Round)
                            drawLine(Color(0xFFE7DEFF), center, Offset(center.x, center.y - 8.dp.toPx()), 1.6.dp.toPx(), StrokeCap.Round)
                        }
                    }
                    HyperOsClockFace.PIXEL_DUAL_DIAL -> {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("09", fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 10.sp, color = Color.White)
                                Text("41", fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 10.sp, color = Color(0xFFCDEEE2))
                            }
                            Canvas(modifier = Modifier.size(13.dp)) {
                                drawCircle(color = Color.White.copy(alpha = 0.35f), style = Stroke(2.dp.toPx()))
                                drawArc(Color(0xFFCDEEE2), -90f, 180f, false, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
                            }
                        }
                    }
                    HyperOsClockFace.KINETIC_MONO -> {
                        Text("09:41:28", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.White)
                    }
                    HyperOsClockFace.RHOMBUS_PRISM -> {
                        Box(contentAlignment = Alignment.Center) {
                            Canvas(modifier = Modifier.size(width = 46.dp, height = 26.dp)) {
                                val p = Path().apply {
                                    moveTo(size.width * 0.5f, 0f)
                                    lineTo(size.width, size.height * 0.5f)
                                    lineTo(size.width * 0.5f, size.height)
                                    lineTo(0f, size.height * 0.5f)
                                    close()
                                }
                                drawPath(p, Color.White.copy(alpha = 0.85f), style = Stroke(1.4.dp.toPx()))
                            }
                            Text("09:41", fontFamily = OrbitronFontFamily, fontWeight = FontWeight.Bold, fontSize = 9.5.sp, color = Color.White)
                        }
                    }
                    HyperOsClockFace.RHOMBUS_DUAL_ROW -> {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("09", fontFamily = SyneFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            Text("◆", fontSize = 8.sp, color = Color(0xFFE7DEFF))
                            Text("41", fontFamily = SyneFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFE7DEFF))
                        }
                    }
                    HyperOsClockFace.CALLIGRAPHY_SEAL -> {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("09", fontFamily = CormorantFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 11.sp, color = Color.White)
                                Text("41", fontFamily = CormorantFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 11.sp, color = Color(0xFFE7DEFF))
                            }
                            Surface(color = Color(0xFFB33A2E), shape = RoundedCornerShape(4.dp)) {
                                Text("15", fontFamily = PlayfairFontFamily, fontSize = 7.5.sp, color = Color.White, modifier = Modifier.padding(horizontal = 3.dp, vertical = 3.dp))
                            }
                        }
                    }
                    else -> {
                        RenderExtendedClockFaceThumbnail(
                            face = face,
                            accentColor = accentColor
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = face.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
