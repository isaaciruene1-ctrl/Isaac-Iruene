package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OutfitFontFamily

private val GoogleBlue = Color(0xFF4285F4)
private val GoogleRed = Color(0xFFEA4335)
private val GoogleYellow = Color(0xFFFBBC05)
private val GoogleGreen = Color(0xFF34A853)
private val PixelPorcelain = Color(0xFFF8F9FA)

/**
 * Custom Google & Pixel-inspired geometric mark for BACKDROP:
 * Playful tilted Pixel canvas frames accented in Google Blue, Red, Yellow, and Green
 * around a clean minimalist peak & sun horizon.
 */
@Composable
fun BackdropLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Back-left tilted Google Red & Yellow Pixel sheet
        rotate(degrees = -10f, pivot = Offset(w * 0.45f, h * 0.50f)) {
            drawRoundRect(
                brush = Brush.linearGradient(listOf(GoogleRed, GoogleYellow)),
                topLeft = Offset(w * 0.12f, h * 0.14f),
                size = Size(w * 0.62f, h * 0.72f),
                cornerRadius = CornerRadius(w * 0.18f, w * 0.18f)
            )
        }

        // Front-right upright Pixel card in Google Blue -> Green
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(GoogleBlue, GoogleGreen),
                start = Offset(w * 0.25f, h * 0.10f),
                end = Offset(w * 0.90f, h * 0.88f)
            ),
            topLeft = Offset(w * 0.24f, h * 0.10f),
            size = Size(w * 0.68f, h * 0.78f),
            cornerRadius = CornerRadius(w * 0.20f, w * 0.20f)
        )

        // Subtle crisp Porcelain rim
        drawRoundRect(
            color = PixelPorcelain.copy(alpha = 0.88f),
            topLeft = Offset(w * 0.24f, h * 0.10f),
            size = Size(w * 0.68f, h * 0.78f),
            cornerRadius = CornerRadius(w * 0.20f, w * 0.20f),
            style = Stroke(width = w * 0.045f)
        )

        // Google Yellow sun orb
        drawCircle(
            color = GoogleYellow,
            radius = w * 0.085f,
            center = Offset(w * 0.70f, h * 0.30f)
        )

        // Minimalist alpine ridge inside front frame
        val mountain = Path().apply {
            moveTo(w * 0.32f, h * 0.74f)
            lineTo(w * 0.52f, h * 0.44f)
            lineTo(w * 0.64f, h * 0.60f)
            lineTo(w * 0.74f, h * 0.49f)
            lineTo(w * 0.84f, h * 0.74f)
            close()
        }
        drawPath(
            path = mountain,
            color = PixelPorcelain.copy(alpha = 0.96f)
        )
    }
}

@Composable
fun BackdropWordmark(
    modifier: Modifier = Modifier,
    markSize: Dp = 30.dp,
    showTagline: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BackdropLogoMark(size = markSize)
        Column {
            Text(
                text = "BACKDROP",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                letterSpacing = 1.6.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (showTagline) {
                Text(
                    text = "YOUR SCREEN. YOUR CANVAS.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.1.sp
                )
            }
        }
    }
}
