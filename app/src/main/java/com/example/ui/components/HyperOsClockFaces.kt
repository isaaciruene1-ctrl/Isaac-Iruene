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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.ui.theme.ShantellSansFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
    opacity: Float,
    showDateAndWeather: Boolean,
    colAlignment: Alignment.Horizontal,
    textAlign: TextAlign
) {
    val shadow = Shadow(
        color = Color.Black.copy(alpha = 0.36f * opacity),
        offset = Offset(0f, 6f),
        blurRadius = 16f
    )

    when (face) {
        HyperOsClockFace.MAGAZINE_STACKED -> {
            if (showDateAndWeather) {
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
                        color = primaryColor.copy(alpha = 0.90f),
                        style = TextStyle(shadow = shadow)
                    )
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(1.5.dp)
                            .background(primaryColor.copy(alpha = 0.70f))
                    )
                    Text(
                        text = "21°",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = (12f * scale).sp,
                        color = primaryColor.copy(alpha = 0.90f),
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
        }

        HyperOsClockFace.POSTER_SPLIT -> {
            if (showDateAndWeather) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${parts.dateLabel}  •  21° CLEAR",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = (12f * scale).sp,
                        letterSpacing = 2.8.sp,
                        color = primaryColor.copy(alpha = 0.88f),
                        style = TextStyle(shadow = shadow)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((6f * scale).dp)
            ) {
                StyledClockText(
                    text = parts.hours,
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    weightLevel = weightLevel,
                    fontSize = (126f * scale).sp,
                    lineHeight = (112f * scale).sp,
                    letterSpacing = (-3).sp,
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
                            .background(secondaryColor.copy(alpha = 0.92f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size((8f * scale).dp)
                            .background(primaryColor.copy(alpha = 0.92f), CircleShape)
                    )
                }
                StyledClockText(
                    text = parts.minutes,
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    weightLevel = weightLevel,
                    fontSize = (126f * scale).sp,
                    lineHeight = (112f * scale).sp,
                    letterSpacing = (-3).sp,
                    finish = if (finish == HyperOsClockFinish.SOLID) HyperOsClockFinish.OUTLINE else finish,
                    primaryColor = secondaryColor,
                    secondaryColor = primaryColor,
                    textAlign = textAlign
                )
            }
        }

        HyperOsClockFace.CLASSIC_HORIZON -> {
            if (showDateAndWeather) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = parts.dateLabel,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = (14f * scale).sp,
                        letterSpacing = 2.sp,
                        color = primaryColor.copy(alpha = 0.92f),
                        style = TextStyle(shadow = shadow)
                    )
                    Icon(
                        imageVector = Icons.Rounded.WbSunny,
                        contentDescription = null,
                        tint = secondaryColor,
                        modifier = Modifier.size((15f * scale).dp)
                    )
                    Text(
                        text = "21°",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = (14f * scale).sp,
                        color = primaryColor.copy(alpha = 0.92f),
                        style = TextStyle(shadow = shadow)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            StyledClockText(
                text = "${parts.hours}:${parts.minutes}",
                fontFamily = fontFamily,
                fontWeight = fontWeight,
                weightLevel = weightLevel,
                fontSize = (88f * scale).sp,
                lineHeight = (88f * scale).sp,
                letterSpacing = (-2.5).sp,
                finish = finish,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                textAlign = textAlign
            )
        }

        HyperOsClockFace.ATELIER_SERIF -> {
            if (showDateAndWeather) {
                Text(
                    text = parts.dateLabel.lowercase(Locale.getDefault())
                        .replaceFirstChar { it.titlecase(Locale.getDefault()) },
                    fontFamily = CormorantFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = (19f * scale).sp,
                    letterSpacing = 1.2.sp,
                    color = primaryColor.copy(alpha = 0.92f),
                    style = TextStyle(shadow = shadow)
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            StyledClockText(
                text = "${parts.hours}:${parts.minutes}",
                fontFamily = fontFamily,
                fontWeight = fontWeight,
                weightLevel = weightLevel,
                fontSize = (94f * scale).sp,
                lineHeight = (90f * scale).sp,
                letterSpacing = (-1).sp,
                finish = finish,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                textAlign = textAlign
            )
            if (showDateAndWeather) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .width(46.dp)
                        .height(1.5.dp)
                        .background(secondaryColor.copy(alpha = 0.85f))
                )
            }
        }

        HyperOsClockFace.DOODLE_PLAY -> {
            if (showDateAndWeather) {
                Surface(
                    color = secondaryColor.copy(alpha = 0.24f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .rotate(-3f)
                        .border(1.5.dp, primaryColor.copy(alpha = 0.55f), RoundedCornerShape(50))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = secondaryColor,
                            modifier = Modifier.size((13f * scale).dp)
                        )
                        Text(
                            text = parts.dateLabel,
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = (12f * scale).sp,
                            color = primaryColor,
                            style = TextStyle(shadow = shadow)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(width = (220f * scale).dp, height = (130f * scale).dp)) {
                    // Hand-drawn wavy doodle arc & playful star sparkles
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
                            fontSize = (94f * scale).sp,
                            lineHeight = (94f * scale).sp,
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
                        fontSize = (72f * scale).sp,
                        color = secondaryColor,
                        style = TextStyle(shadow = shadow)
                    )
                    Box(modifier = Modifier.rotate(4f)) {
                        StyledClockText(
                            text = parts.minutes,
                            fontFamily = fontFamily,
                            fontWeight = fontWeight,
                            weightLevel = weightLevel,
                            fontSize = (94f * scale).sp,
                            lineHeight = (94f * scale).sp,
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
                        color = primaryColor.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .rotate(-4f)
                            .border(2.dp, primaryColor.copy(alpha = 0.65f), RoundedCornerShape(24.dp))
                    ) {
                        Box(modifier = Modifier.padding(horizontal = (14f * scale).dp, vertical = (2f * scale).dp)) {
                            StyledClockText(
                                text = parts.hours,
                                fontFamily = fontFamily,
                                fontWeight = fontWeight,
                                weightLevel = weightLevel,
                                fontSize = (78f * scale).sp,
                                lineHeight = (78f * scale).sp,
                                letterSpacing = 0.sp,
                                finish = finish,
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Surface(
                        color = secondaryColor.copy(alpha = 0.28f),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .rotate(4f)
                            .border(2.dp, secondaryColor.copy(alpha = 0.70f), RoundedCornerShape(24.dp))
                    ) {
                        Box(modifier = Modifier.padding(horizontal = (14f * scale).dp, vertical = (2f * scale).dp)) {
                            StyledClockText(
                                text = parts.minutes,
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
                        }
                    }
                }
                if (showDateAndWeather) {
                    Text(
                        text = "${parts.dateLabel}  ★  21°",
                        fontFamily = ShantellSansFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = (13f * scale).sp,
                        color = primaryColor.copy(alpha = 0.92f),
                        style = TextStyle(shadow = shadow)
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
                        HyperOsClockFinish.TRANSLUCENT -> 0.55f
                        HyperOsClockFinish.BLUR_GLASS -> 0.35f
                        else -> 0.18f
                    }
                    if (finish != HyperOsClockFinish.OUTLINE) {
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
                        color = primaryColor.copy(alpha = 0.85f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = (2.6f * strokeFactor).dp.toPx())
                    )
                    // 12 crisp hour markers
                    for (i in 0 until 12) {
                        val angleRad = (i * 30.0 - 90.0) * PI / 180.0
                        val inner = if (i % 3 == 0) radius * 0.78f else radius * 0.86f
                        val outer = radius * 0.93f
                        val sw = if (i % 3 == 0) (3.2f * strokeFactor).dp.toPx() else (1.6f * strokeFactor).dp.toPx()
                        drawLine(
                            color = (if (i % 3 == 0) secondaryColor else primaryColor).copy(alpha = 0.9f),
                            start = Offset(center.x + (inner * cos(angleRad)).toFloat(), center.y + (inner * sin(angleRad)).toFloat()),
                            end = Offset(center.x + (outer * cos(angleRad)).toFloat(), center.y + (outer * sin(angleRad)).toFloat()),
                            strokeWidth = sw,
                            cap = StrokeCap.Round
                        )
                    }
                    // Hour hand
                    val hourAngle = (((parts.hourInt % 12) + parts.minuteInt / 60f) * 30f - 90f) * PI / 180.0
                    drawLine(
                        color = primaryColor,
                        start = center,
                        end = Offset(center.x + (radius * 0.52f * cos(hourAngle)).toFloat(), center.y + (radius * 0.52f * sin(hourAngle)).toFloat()),
                        strokeWidth = (5.5f * strokeFactor).dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    // Minute hand
                    val minAngle = ((parts.minuteInt + parts.secondInt / 60f) * 6f - 90f) * PI / 180.0
                    drawLine(
                        color = primaryColor,
                        start = center,
                        end = Offset(center.x + (radius * 0.74f * cos(minAngle)).toFloat(), center.y + (radius * 0.74f * sin(minAngle)).toFloat()),
                        strokeWidth = (3.6f * strokeFactor).dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    // Second hand
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
                if (showDateAndWeather) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "${parts.dateLabel} • 21°",
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = (11f * scale).sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
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
                        fontSize = (88f * scale).sp,
                        lineHeight = (78f * scale).sp,
                        letterSpacing = (-2.5).sp,
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
                        fontSize = (88f * scale).sp,
                        lineHeight = (78f * scale).sp,
                        letterSpacing = (-2.5).sp,
                        finish = finish,
                        primaryColor = secondaryColor,
                        secondaryColor = primaryColor,
                        textAlign = textAlign
                    )
                }
                if (showDateAndWeather) {
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
                                    color = primaryColor.copy(alpha = 0.25f),
                                    style = Stroke(width = 3.5.dp.toPx())
                                )
                                drawArc(
                                    color = secondaryColor,
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
                                color = primaryColor,
                                style = TextStyle(shadow = shadow)
                            )
                        }
                        Surface(
                            color = primaryColor.copy(alpha = 0.20f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = parts.dateLabel.substringBefore(","),
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = (11f * scale).sp,
                                color = primaryColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        HyperOsClockFace.KINETIC_MONO -> {
            if (showDateAndWeather) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(50),
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = parts.dateLabel,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = (11f * scale).sp,
                            letterSpacing = 1.2.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "•",
                            fontSize = (11f * scale).sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "21°C CLEAR",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = (11f * scale).sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
            Row(verticalAlignment = Alignment.Bottom) {
                StyledClockText(
                    text = "${parts.hours}:${parts.minutes}",
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    weightLevel = weightLevel,
                    fontSize = (78f * scale).sp,
                    lineHeight = (78f * scale).sp,
                    letterSpacing = (-3).sp,
                    finish = finish,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    textAlign = textAlign
                )
                Text(
                    text = ":${parts.seconds}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = (24f * scale).sp,
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
                    drawPath(
                        path = diamond,
                        brush = Brush.linearGradient(
                            listOf(primaryColor.copy(alpha = 0.14f), secondaryColor.copy(alpha = 0.12f))
                        )
                    )
                    drawPath(
                        path = diamond,
                        brush = Brush.linearGradient(
                            listOf(primaryColor.copy(alpha = 0.85f), secondaryColor.copy(alpha = 0.85f))
                        ),
                        style = Stroke(width = 2.dp.toPx(), join = StrokeJoin.Miter)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (showDateAndWeather) {
                        Text(
                            text = parts.dateLabel,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = (10f * scale).sp,
                            letterSpacing = 2.sp,
                            color = secondaryColor,
                            style = TextStyle(shadow = shadow)
                        )
                    }
                    StyledClockText(
                        text = "${parts.hours}:${parts.minutes}",
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        weightLevel = weightLevel,
                        fontSize = (62f * scale).sp,
                        lineHeight = (62f * scale).sp,
                        letterSpacing = 1.sp,
                        finish = finish,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        textAlign = TextAlign.Center
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
                if (showDateAndWeather) {
                    Surface(
                        color = Color(0xFFB33A2E).copy(alpha = 0.88f),
                        shape = RoundedCornerShape(10.dp),
                        tonalElevation = 2.dp,
                        modifier = Modifier.border(
                            width = 1.5.dp,
                            color = Color.White.copy(alpha = 0.55f),
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
                                text = "21°",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = (10f * scale).sp,
                                color = Color.White.copy(alpha = 0.90f)
                            )
                        }
                    }
                }
            }
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
    val ambientShadow = Shadow(
        color = Color.Black.copy(alpha = 0.38f),
        offset = Offset(0f, 6f),
        blurRadius = 18f
    )
    when (finish) {
        HyperOsClockFinish.SOLID -> {
            Text(
                text = text,
                textAlign = textAlign,
                style = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    letterSpacing = letterSpacing,
                    color = primaryColor,
                    shadow = ambientShadow
                )
            )
        }

        HyperOsClockFinish.TRANSLUCENT -> {
            Text(
                text = text,
                textAlign = textAlign,
                style = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    letterSpacing = letterSpacing,
                    color = primaryColor.copy(alpha = 0.54f),
                    shadow = Shadow(
                        color = secondaryColor.copy(alpha = 0.35f),
                        offset = Offset(0f, 3f),
                        blurRadius = 14f
                    )
                )
            )
        }

        HyperOsClockFinish.BLEND -> {
            Text(
                text = text,
                textAlign = textAlign,
                style = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    letterSpacing = letterSpacing,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.92f),
                            secondaryColor.copy(alpha = 0.86f),
                            primaryColor.copy(alpha = 0.78f)
                        )
                    ),
                    shadow = ambientShadow
                )
            )
        }

        HyperOsClockFinish.OUTLINE -> {
            val strokePx = ((weightLevel / 100f) * 1.1f).coerceIn(2.2f, 9.5f)
            Text(
                text = text,
                textAlign = textAlign,
                style = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    letterSpacing = letterSpacing,
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor, secondaryColor)
                    ),
                    drawStyle = Stroke(
                        width = strokePx,
                        join = StrokeJoin.Round,
                        cap = StrokeCap.Round
                    ),
                    shadow = ambientShadow
                )
            )
        }

        HyperOsClockFinish.BLUR_GLASS -> {
            Text(
                text = text,
                textAlign = textAlign,
                style = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = fontWeight,
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    letterSpacing = letterSpacing,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.94f),
                            primaryColor.copy(alpha = 0.72f)
                        )
                    ),
                    shadow = Shadow(
                        color = Color.White.copy(alpha = 0.25f),
                        offset = Offset(0f, 2f),
                        blurRadius = 12f
                    )
                )
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
        tonalElevation = if (selected) 4.dp else 1.dp,
        modifier = modifier.size(width = 92.dp, height = 88.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
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
                                MaterialTheme.colorScheme.inverseSurface,
                                accentColor.copy(alpha = 0.55f)
                            )
                        ),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (face) {
                    HyperOsClockFace.MAGAZINE_STACKED -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("09", fontFamily = OswaldFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 14.sp, color = Color.White)
                            Text("41", fontFamily = OswaldFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 14.sp, color = Color.White)
                        }
                    }
                    HyperOsClockFace.POSTER_SPLIT -> {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("09", fontFamily = BigShouldersFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color.White)
                            Text(":", fontFamily = BigShouldersFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = accentColor)
                            Text(
                                "41",
                                style = TextStyle(
                                    fontFamily = BigShouldersFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    color = Color.White,
                                    drawStyle = Stroke(width = 2.5f)
                                )
                            )
                        }
                    }
                    HyperOsClockFace.CLASSIC_HORIZON -> {
                        Text("09:41", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                    HyperOsClockFace.ATELIER_SERIF -> {
                        Text("09:41", fontFamily = PlayfairFontFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                    }
                    HyperOsClockFace.DOODLE_PLAY -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("09", fontFamily = FredokaFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White, modifier = Modifier.rotate(-6f))
                            Text(":", fontFamily = FredokaFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFFFD8C8))
                            Text("41", fontFamily = FredokaFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFE7DEFF), modifier = Modifier.rotate(6f))
                        }
                    }
                    HyperOsClockFace.CUTOUT_POP -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Surface(color = Color.White.copy(alpha = 0.28f), shape = RoundedCornerShape(6.dp), modifier = Modifier.rotate(-5f)) {
                                Text("09", fontFamily = ShantellSansFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                            Surface(color = accentColor.copy(alpha = 0.45f), shape = RoundedCornerShape(6.dp), modifier = Modifier.rotate(5f)) {
                                Text("41", fontFamily = ShantellSansFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                    }
                    HyperOsClockFace.ANALOG_MINIMAL -> {
                        Canvas(modifier = Modifier.size(30.dp)) {
                            drawCircle(color = Color.White, style = Stroke(width = 2.dp.toPx()))
                            drawLine(Color.White, center, Offset(center.x + 6.dp.toPx(), center.y - 4.dp.toPx()), 2.dp.toPx(), StrokeCap.Round)
                            drawLine(Color(0xFFE7DEFF), center, Offset(center.x, center.y - 9.dp.toPx()), 1.6.dp.toPx(), StrokeCap.Round)
                        }
                    }
                    HyperOsClockFace.PIXEL_DUAL_DIAL -> {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("09", fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 11.sp, color = Color.White)
                                Text("41", fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 11.sp, color = Color(0xFFCDEEE2))
                            }
                            Canvas(modifier = Modifier.size(14.dp)) {
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
                            Canvas(modifier = Modifier.size(width = 48.dp, height = 28.dp)) {
                                val p = Path().apply {
                                    moveTo(size.width * 0.5f, 0f)
                                    lineTo(size.width, size.height * 0.5f)
                                    lineTo(size.width * 0.5f, size.height)
                                    lineTo(0f, size.height * 0.5f)
                                    close()
                                }
                                drawPath(p, Color.White.copy(alpha = 0.85f), style = Stroke(1.4.dp.toPx()))
                            }
                            Text("09:41", fontFamily = OrbitronFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.White)
                        }
                    }
                    HyperOsClockFace.CALLIGRAPHY_SEAL -> {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("09", fontFamily = CormorantFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 12.sp, color = Color.White)
                                Text("41", fontFamily = CormorantFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 12.sp, color = Color(0xFFE7DEFF))
                            }
                            Surface(color = Color(0xFFB33A2E), shape = RoundedCornerShape(4.dp)) {
                                Text("15", fontFamily = PlayfairFontFamily, fontSize = 8.sp, color = Color.White, modifier = Modifier.padding(horizontal = 3.dp, vertical = 4.dp))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
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
