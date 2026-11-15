package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OutfitFontFamily

/**
 * Custom geometric canvas mark for BACKDROP — two overlapping translucent
 * canvas frames with a crisp mountain horizon peak inside.
 */
@Composable
fun BackdropLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    accentColor: Color = MaterialTheme.colorScheme.tertiary
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Back tilted/offset pastel canvas frame
        drawRoundRect(
            brush = Brush.linearGradient(
                listOf(primaryColor.copy(alpha = 0.42f), secondaryColor.copy(alpha = 0.42f))
            ),
            topLeft = Offset(w * 0.08f, h * 0.14f),
            size = Size(w * 0.68f, h * 0.78f),
            cornerRadius = CornerRadius(w * 0.16f, w * 0.16f)
        )

        // Front translucent canvas frame
        drawRoundRect(
            brush = Brush.linearGradient(
                listOf(primaryColor.copy(alpha = 0.88f), accentColor.copy(alpha = 0.72f))
            ),
            topLeft = Offset(w * 0.24f, h * 0.08f),
            size = Size(w * 0.68f, h * 0.78f),
            cornerRadius = CornerRadius(w * 0.16f, w * 0.16f)
        )

        // Crisp inner frame highlight
        drawRoundRect(
            color = Color.White.copy(alpha = 0.7f),
            topLeft = Offset(w * 0.24f, h * 0.08f),
            size = Size(w * 0.68f, h * 0.78f),
            cornerRadius = CornerRadius(w * 0.16f, w * 0.16f),
            style = Stroke(width = w * 0.045f)
        )

        // Horizon mountain peak inside the canvas
        val mountain = Path().apply {
            moveTo(w * 0.34f, h * 0.72f)
            lineTo(w * 0.54f, h * 0.42f)
            lineTo(w * 0.66f, h * 0.58f)
            lineTo(w * 0.75f, h * 0.48f)
            lineTo(w * 0.83f, h * 0.72f)
            close()
        }
        drawPath(
            path = mountain,
            color = Color.White.copy(alpha = 0.92f)
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
        androidx.compose.foundation.layout.Column {
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
