package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UiStyleMode
import com.example.ui.theme.LocalDarkThemeFlag
import com.example.ui.theme.LocalUiStyleMode
import com.example.ui.theme.OutfitFontFamily

/**
 * Unified Backdrops circular emblem matching the Android adaptive launcher icon
 * (`ic_pixel_backdrop_art`) across Home, Nav Rail, Profile, and Settings.
 */
@Composable
fun BackdropLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    val isGlass = LocalUiStyleMode.current == UiStyleMode.FROSTED_GLASS_OS
    val isDark = LocalDarkThemeFlag.current
    val rimColor = when {
        isGlass && isDark -> Color.White.copy(alpha = 0.34f)
        isGlass -> Color.White.copy(alpha = 0.85f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    }
    Surface(
        shape = CircleShape,
        color = Color(0xFFFCFCFC),
        border = BorderStroke(1.dp, rimColor),
        shadowElevation = if (isGlass) 4.dp else 1.dp,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .testTag("backdrop_logo_mark")
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_pixel_backdrop_art),
            contentDescription = "Backdrops logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
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
        if (markSize > 0.dp) {
            BackdropLogoMark(size = markSize)
        }
        Column {
            Text(
                text = "Backdrops",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                letterSpacing = 0.2.sp,
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
