package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.depth.ClockHorizontalAlign
import com.example.ui.theme.CormorantFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OswaldFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.SyneFontFamily
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class HyperOsClockFace(
    val label: String,
    val subtitle: String,
    val recommendedFont: HyperOsClockFont
) {
    MAGAZINE_STACKED("Magazine", "Stacked bold editorial", HyperOsClockFont.OSWALD),
    CLASSIC_HORIZON("Editorial", "Wide classic lock display", HyperOsClockFont.OUTFIT),
    ATELIER_SERIF("Atelier", "Refined serif pairing", HyperOsClockFont.CORMORANT),
    VERTICAL_SEAL("Studio Seal", "Vertical time with date badge", HyperOsClockFont.SYNE),
    KINETIC_MONO("Mono", "Tabular precision & seconds", HyperOsClockFont.JETBRAINS_MONO)
}

enum class HyperOsClockFont(
    val label: String,
    val family: FontFamily
) {
    OSWALD("Oswald", OswaldFontFamily),
    CORMORANT("Cormorant", CormorantFontFamily),
    OUTFIT("Outfit", OutfitFontFamily),
    SYNE("Syne", SyneFontFamily),
    SPACE_GROTESK("Space", SpaceGroteskFontFamily),
    JETBRAINS_MONO("Mono", JetBrainsMonoFontFamily)
}

enum class HyperOsClockFinish(val label: String) {
    SOLID("Solid"),
    DUO_GRADIENT("Tonal Gradient")
}

@Composable
fun HyperOsLockClock(
    face: HyperOsClockFace,
    font: HyperOsClockFont,
    finish: HyperOsClockFinish,
    primaryColor: Color,
    secondaryColor: Color,
    scaleFactor: Float = 1.0f,
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

    val (hours, minutes, seconds, dateLabel) = remember(useLiveTime, nowMillis) {
        if (!useLiveTime) {
            ClockTimeParts(
                hours = "09",
                minutes = "41",
                seconds = "28",
                dateLabel = "FRI, NOV 15"
            )
        } else {
            val d = Date(nowMillis)
            val hh = SimpleDateFormat("HH", Locale.getDefault()).format(d)
            val mm = SimpleDateFormat("mm", Locale.getDefault()).format(d)
            val ss = SimpleDateFormat("ss", Locale.getDefault()).format(d)
            val dateStr = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(d).uppercase(Locale.getDefault())
            ClockTimeParts(hh, mm, ss, dateStr)
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

    val scale = scaleFactor.coerceIn(0.75f, 1.35f)
    val shadow = Shadow(
        color = Color.Black.copy(alpha = 0.42f),
        offset = Offset(0f, 6f),
        blurRadius = 16f
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = colAlignment
    ) {
        when (face) {
            HyperOsClockFace.MAGAZINE_STACKED -> {
                if (showDateAndWeather) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = dateLabel,
                            fontFamily = font.family,
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
                    text = hours,
                    fontFamily = font.family,
                    fontWeight = FontWeight.Bold,
                    fontSize = (106f * scale).sp,
                    lineHeight = (92f * scale).sp,
                    letterSpacing = (-2).sp,
                    finish = finish,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    textAlign = textAlign
                )
                StyledClockText(
                    text = minutes,
                    fontFamily = font.family,
                    fontWeight = FontWeight.Bold,
                    fontSize = (106f * scale).sp,
                    lineHeight = (90f * scale).sp,
                    letterSpacing = (-2).sp,
                    finish = finish,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    textAlign = textAlign
                )
            }

            HyperOsClockFace.CLASSIC_HORIZON -> {
                if (showDateAndWeather) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = dateLabel,
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
                    text = "$hours:$minutes",
                    fontFamily = font.family,
                    fontWeight = FontWeight.Bold,
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
                        text = dateLabel.lowercase(Locale.getDefault())
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
                    text = "$hours:$minutes",
                    fontFamily = font.family,
                    fontWeight = FontWeight.Bold,
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

            HyperOsClockFace.VERTICAL_SEAL -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(horizontalAlignment = colAlignment) {
                        StyledClockText(
                            text = hours,
                            fontFamily = font.family,
                            fontWeight = FontWeight.Bold,
                            fontSize = (90f * scale).sp,
                            lineHeight = (80f * scale).sp,
                            letterSpacing = (-2).sp,
                            finish = finish,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            textAlign = textAlign
                        )
                        StyledClockText(
                            text = minutes,
                            fontFamily = font.family,
                            fontWeight = FontWeight.Bold,
                            fontSize = (90f * scale).sp,
                            lineHeight = (80f * scale).sp,
                            letterSpacing = (-2).sp,
                            finish = finish,
                            primaryColor = secondaryColor,
                            secondaryColor = primaryColor,
                            textAlign = textAlign
                        )
                    }
                    if (showDateAndWeather) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            shape = RoundedCornerShape(14.dp),
                            tonalElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = dateLabel.substringBefore(","),
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (11f * scale).sp,
                                    letterSpacing = 1.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Box(
                                    modifier = Modifier
                                        .width(14.dp)
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.outline)
                                )
                                Text(
                                    text = dateLabel.substringAfter(", ").ifBlank { "15" },
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = (11f * scale).sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                text = dateLabel,
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
                        text = "$hours:$minutes",
                        fontFamily = font.family,
                        fontWeight = FontWeight.Bold,
                        fontSize = (78f * scale).sp,
                        lineHeight = (78f * scale).sp,
                        letterSpacing = (-3).sp,
                        finish = finish,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        textAlign = textAlign
                    )
                    Text(
                        text = ":$seconds",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = (24f * scale).sp,
                        color = secondaryColor.copy(alpha = 0.94f),
                        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp),
                        style = TextStyle(shadow = shadow)
                    )
                }
            }
        }
    }
}

@Composable
private fun StyledClockText(
    text: String,
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    letterSpacing: TextUnit,
    finish: HyperOsClockFinish,
    primaryColor: Color,
    secondaryColor: Color,
    textAlign: TextAlign
) {
    val ambientShadow = Shadow(
        color = Color.Black.copy(alpha = 0.40f),
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

        HyperOsClockFinish.DUO_GRADIENT -> {
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
                    shadow = ambientShadow
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
        modifier = modifier.size(width = 88.dp, height = 84.dp)
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
                    HyperOsClockFace.CLASSIC_HORIZON -> {
                        Text("09:41", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                    HyperOsClockFace.ATELIER_SERIF -> {
                        Text("09:41", fontFamily = CormorantFontFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                    }
                    HyperOsClockFace.VERTICAL_SEAL -> {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("09", fontFamily = SyneFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 12.sp, color = Color.White)
                                Text("41", fontFamily = SyneFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 12.sp, color = Color(0xFFE7DEFF))
                            }
                            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(4.dp)) {
                                Text("15", fontFamily = JetBrainsMonoFontFamily, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 3.dp, vertical = 4.dp))
                            }
                        }
                    }
                    HyperOsClockFace.KINETIC_MONO -> {
                        Text("09:41:28", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = face.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

private data class ClockTimeParts(
    val hours: String,
    val minutes: String,
    val seconds: String,
    val dateLabel: String
)
