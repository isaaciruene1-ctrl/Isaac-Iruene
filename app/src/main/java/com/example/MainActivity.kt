package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.data.catalog.WallpaperCatalog
import com.example.ui.BackdropRootApp
import com.example.ui.components.BackdropLogoMark
import com.example.widget.BackdropWidgetContract

class MainActivity : ComponentActivity() {

    private var deepLinkStudioWpId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        parseDeepLinkIntent(intent)

        setContent {
            var showSplashTransition by remember { mutableStateOf(true) }
            val iconScale = remember { Animatable(0.92f) }
            val splashAlpha = remember { Animatable(1f) }

            LaunchedEffect(Unit) {
                iconScale.animateTo(
                    targetValue = 1.24f,
                    animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing)
                )
                splashAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 180)
                )
                showSplashTransition = false
            }

            Box(modifier = Modifier.fillMaxSize()) {
                BackdropRootApp(
                    enableOnboardingGate = true,
                    initialStudioWallpaperId = deepLinkStudioWpId,
                    onStudioDeepLinkConsumed = { deepLinkStudioWpId = null }
                )

                AnimatedVisibility(
                    visible = showSplashTransition,
                    exit = fadeOut(tween(150))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = splashAlpha.value }
                            .background(Color(0xFF0E0D14)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier.graphicsLayer {
                                scaleX = iconScale.value
                                scaleY = iconScale.value
                            }
                        ) {
                            BackdropLogoMark(size = 84.dp)
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        parseDeepLinkIntent(intent)
    }

    private fun parseDeepLinkIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.getBooleanExtra(BackdropWidgetContract.EXTRA_OPEN_STUDIO, false)) {
            val wpId = intent.getStringExtra(BackdropWidgetContract.EXTRA_WALLPAPER_ID)
                ?.takeIf { WallpaperCatalog.getById(it) != null }
                ?: WallpaperCatalog.allWallpapers.firstOrNull()?.id
            deepLinkStudioWpId = wpId
            intent.removeExtra(BackdropWidgetContract.EXTRA_OPEN_STUDIO)
        }
    }
}
