package com.example.ui.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.data.model.UiStyleMode

/**
 * Distinct motion systems for Material 3 vs Liquid Glass (Frosted Glass OS).
 * Neither mode reuses the other's curves, durations, or spring physics.
 */
object AppMotion {

    fun <T> spatialSpring(uiStyle: UiStyleMode, reducedMotion: Boolean = false): SpringSpec<T> {
        if (reducedMotion) {
            return spring(dampingRatio = 0.98f, stiffness = 440f)
        }
        return when (uiStyle) {
            UiStyleMode.MATERIAL_3 -> spring(dampingRatio = 0.88f, stiffness = 560f)
            UiStyleMode.FROSTED_GLASS_OS -> spring(dampingRatio = 0.46f, stiffness = 285f)
        }
    }

    fun <T> emphasisSpring(uiStyle: UiStyleMode, reducedMotion: Boolean = false): SpringSpec<T> {
        if (reducedMotion) {
            return spring(dampingRatio = 0.98f, stiffness = 460f)
        }
        return when (uiStyle) {
            UiStyleMode.MATERIAL_3 -> spring(dampingRatio = 0.90f, stiffness = 620f)
            UiStyleMode.FROSTED_GLASS_OS -> spring(dampingRatio = 0.42f, stiffness = 250f)
        }
    }

    fun <T> glassMorphSpring(uiStyle: UiStyleMode, reducedMotion: Boolean = false): SpringSpec<T> {
        if (reducedMotion) {
            return spring(dampingRatio = 0.96f, stiffness = 440f)
        }
        return when (uiStyle) {
            UiStyleMode.MATERIAL_3 -> spring(dampingRatio = 0.84f, stiffness = 520f)
            UiStyleMode.FROSTED_GLASS_OS -> spring(dampingRatio = 0.44f, stiffness = 240f)
        }
    }

    fun floatEffectsSpec(uiStyle: UiStyleMode, reducedMotion: Boolean = false): FiniteAnimationSpec<Float> {
        if (reducedMotion) return tween(120, easing = FastOutSlowInEasing)
        return when (uiStyle) {
            UiStyleMode.MATERIAL_3 -> spring(dampingRatio = 0.90f, stiffness = 540f)
            UiStyleMode.FROSTED_GLASS_OS -> spring(dampingRatio = 0.54f, stiffness = 290f)
        }
    }

    fun colorSpec(uiStyle: UiStyleMode, reducedMotion: Boolean = false): FiniteAnimationSpec<Color> {
        if (reducedMotion) return tween(110, easing = FastOutSlowInEasing)
        return when (uiStyle) {
            UiStyleMode.MATERIAL_3 -> tween(190, easing = FastOutSlowInEasing)
            UiStyleMode.FROSTED_GLASS_OS -> spring(dampingRatio = 0.68f, stiffness = 310f)
        }
    }

    fun screenEnter(uiStyle: UiStyleMode, reducedMotion: Boolean): EnterTransition {
        if (reducedMotion) return fadeIn(tween(140))
        return when (uiStyle) {
            UiStyleMode.MATERIAL_3 -> {
                fadeIn(tween(200, easing = FastOutSlowInEasing)) +
                    slideInVertically(
                        animationSpec = spring(dampingRatio = 0.86f, stiffness = 520f)
                    ) { it / 26 }
            }
            UiStyleMode.FROSTED_GLASS_OS -> {
                fadeIn(tween(240, easing = FastOutSlowInEasing)) +
                    scaleIn(
                        initialScale = 0.89f,
                        animationSpec = spring(dampingRatio = 0.48f, stiffness = 280f)
                    )
            }
        }
    }

    fun screenExit(uiStyle: UiStyleMode, reducedMotion: Boolean): ExitTransition {
        if (reducedMotion) return fadeOut(tween(120))
        return when (uiStyle) {
            UiStyleMode.MATERIAL_3 -> {
                fadeOut(tween(160, easing = FastOutSlowInEasing)) +
                    slideOutVertically(tween(170, easing = FastOutSlowInEasing)) { -it / 30 }
            }
            UiStyleMode.FROSTED_GLASS_OS -> {
                fadeOut(tween(180, easing = FastOutSlowInEasing)) +
                    scaleOut(
                        targetScale = 1.045f,
                        animationSpec = tween(180, easing = FastOutSlowInEasing)
                    )
            }
        }
    }

    fun drawerEnter(uiStyle: UiStyleMode, reducedMotion: Boolean): EnterTransition {
        if (reducedMotion) return fadeIn(tween(120))
        return when (uiStyle) {
            UiStyleMode.MATERIAL_3 -> {
                fadeIn(tween(170)) +
                    expandVertically(spring(dampingRatio = 0.88f, stiffness = 540f))
            }
            UiStyleMode.FROSTED_GLASS_OS -> {
                fadeIn(tween(220)) +
                    expandVertically(spring(dampingRatio = 0.52f, stiffness = 290f)) +
                    scaleIn(initialScale = 0.93f, animationSpec = spring(dampingRatio = 0.48f, stiffness = 300f))
            }
        }
    }

    fun drawerExit(uiStyle: UiStyleMode, reducedMotion: Boolean): ExitTransition {
        if (reducedMotion) return fadeOut(tween(100))
        return when (uiStyle) {
            UiStyleMode.MATERIAL_3 -> {
                fadeOut(tween(140)) + shrinkVertically(tween(170, easing = FastOutSlowInEasing))
            }
            UiStyleMode.FROSTED_GLASS_OS -> {
                fadeOut(tween(160)) + shrinkVertically(spring(dampingRatio = 0.72f, stiffness = 360f))
            }
        }
    }
}

class StudioHapticsController(
    private val haptic: HapticFeedback,
    private val enabled: Boolean
) {
    private var lastTickNs: Long = 0L

    fun performTick(minIntervalMs: Long = 45L) {
        if (!enabled) return
        val now = System.nanoTime()
        if ((now - lastTickNs) >= minIntervalMs * 1_000_000L) {
            lastTickNs = now
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    fun performConfirm() {
        if (!enabled) return
        lastTickNs = System.nanoTime()
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    fun performSnapDetent() {
        if (!enabled) return
        val now = System.nanoTime()
        if ((now - lastTickNs) >= 65L * 1_000_000L) {
            lastTickNs = now
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun performBoundaryBounce() {
        if (!enabled) return
        val now = System.nanoTime()
        if ((now - lastTickNs) >= 120L * 1_000_000L) {
            lastTickNs = now
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
}

@Composable
fun rememberStudioHaptics(): StudioHapticsController {
    val haptic = LocalHapticFeedback.current
    val enabled = LocalHapticsEnabled.current
    return remember(haptic, enabled) { StudioHapticsController(haptic, enabled) }
}
