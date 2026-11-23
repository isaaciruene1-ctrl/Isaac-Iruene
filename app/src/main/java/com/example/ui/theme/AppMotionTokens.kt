package com.example.ui.theme

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.data.model.UiStyleMode

/**
 * Distinct motion systems for Material 3 vs Liquid Glass (Frosted Glass OS).
 * Neither mode reuses the other's curves, durations, or spring physics.
 */
object AppMotion {

    fun <T> spatialSpring(
        uiStyle: UiStyleMode,
        reducedMotion: Boolean = false,
        speedMultiplier: Float = 1.0f
    ): SpringSpec<T> {
        if (reducedMotion) {
            return spring(dampingRatio = 0.98f, stiffness = 440f)
        }
        val speed = speedMultiplier.coerceIn(0.6f, 1.5f)
        return when (uiStyle) {
            UiStyleMode.MATERIAL_3 -> spring(dampingRatio = 0.88f, stiffness = 560f * speed)
            UiStyleMode.FROSTED_GLASS_OS -> spring(dampingRatio = 0.44f, stiffness = 275f * speed)
        }
    }

    fun <T> emphasisSpring(
        uiStyle: UiStyleMode,
        reducedMotion: Boolean = false,
        speedMultiplier: Float = 1.0f
    ): SpringSpec<T> {
        if (reducedMotion) {
            return spring(dampingRatio = 0.98f, stiffness = 460f)
        }
        val speed = speedMultiplier.coerceIn(0.6f, 1.5f)
        return when (uiStyle) {
            UiStyleMode.MATERIAL_3 -> spring(dampingRatio = 0.90f, stiffness = 620f * speed)
            UiStyleMode.FROSTED_GLASS_OS -> spring(dampingRatio = 0.40f, stiffness = 245f * speed)
        }
    }

    fun <T> glassMorphSpring(
        uiStyle: UiStyleMode,
        reducedMotion: Boolean = false,
        speedMultiplier: Float = 1.0f
    ): SpringSpec<T> {
        if (reducedMotion) {
            return spring(dampingRatio = 0.96f, stiffness = 440f)
        }
        val speed = speedMultiplier.coerceIn(0.6f, 1.5f)
        return when (uiStyle) {
            UiStyleMode.MATERIAL_3 -> spring(dampingRatio = 0.84f, stiffness = 520f * speed)
            UiStyleMode.FROSTED_GLASS_OS -> spring(dampingRatio = 0.42f, stiffness = 235f * speed)
        }
    }

    fun floatEffectsSpec(
        uiStyle: UiStyleMode,
        reducedMotion: Boolean = false,
        speedMultiplier: Float = 1.0f
    ): FiniteAnimationSpec<Float> {
        if (reducedMotion) return tween(120, easing = FastOutSlowInEasing)
        val speed = speedMultiplier.coerceIn(0.6f, 1.5f)
        return when (uiStyle) {
            UiStyleMode.MATERIAL_3 -> spring(dampingRatio = 0.90f, stiffness = 540f * speed)
            UiStyleMode.FROSTED_GLASS_OS -> spring(dampingRatio = 0.52f, stiffness = 285f * speed)
        }
    }

    fun colorSpec(
        uiStyle: UiStyleMode,
        reducedMotion: Boolean = false,
        speedMultiplier: Float = 1.0f
    ): FiniteAnimationSpec<Color> {
        if (reducedMotion) return tween(110, easing = FastOutSlowInEasing)
        val speed = speedMultiplier.coerceIn(0.6f, 1.5f)
        return when (uiStyle) {
            UiStyleMode.MATERIAL_3 -> tween((190 / speed).toInt().coerceIn(90, 320), easing = FastOutSlowInEasing)
            UiStyleMode.FROSTED_GLASS_OS -> spring(dampingRatio = 0.68f, stiffness = 310f * speed)
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

/**
 * Hardware-backed haptic engine combining Android Vibrator / VibratorManager `VibrationEffect`
 * (predefined crisp detents on API 29+, one-shot fallback API 24-28) with Compose HapticFeedback.
 */
class StudioHapticsController(
    private val context: Context?,
    private val haptic: HapticFeedback,
    private val enabled: Boolean
) {
    private var lastTickNs: Long = 0L

    private val vibrator: Vibrator? by lazy(LazyThreadSafetyMode.NONE) {
        val ctx = context?.applicationContext ?: return@lazy null
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val mgr = ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                mgr?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        }.getOrNull()
    }

    private fun vibrateHardware(effectIdApi29: Int, durationMs: Long, amplitude: Int, fallbackCompose: HapticFeedbackType) {
        val vib = vibrator
        val firedHardware = if (vib != null && runCatching { vib.hasVibrator() }.getOrDefault(false)) {
            runCatching {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        vib.vibrate(VibrationEffect.createPredefined(effectIdApi29))
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        vib.vibrate(VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255)))
                    }
                    else -> {
                        @Suppress("DEPRECATION")
                        vib.vibrate(durationMs)
                    }
                }
                true
            }.getOrDefault(false)
        } else {
            false
        }
        if (!firedHardware) {
            runCatching { haptic.performHapticFeedback(fallbackCompose) }
        }
    }

    fun performTick(minIntervalMs: Long = 40L) {
        if (!enabled) return
        val now = System.nanoTime()
        if ((now - lastTickNs) >= minIntervalMs * 1_000_000L) {
            lastTickNs = now
            vibrateHardware(
                effectIdApi29 = VibrationEffect.EFFECT_TICK,
                durationMs = 12L,
                amplitude = 90,
                fallbackCompose = HapticFeedbackType.TextHandleMove
            )
        }
    }

    fun performConfirm() {
        if (!enabled) return
        lastTickNs = System.nanoTime()
        vibrateHardware(
            effectIdApi29 = VibrationEffect.EFFECT_CLICK,
            durationMs = 20L,
            amplitude = 145,
            fallbackCompose = HapticFeedbackType.LongPress
        )
    }

    fun performSnapDetent() {
        if (!enabled) return
        val now = System.nanoTime()
        if ((now - lastTickNs) >= 55L * 1_000_000L) {
            lastTickNs = now
            vibrateHardware(
                effectIdApi29 = VibrationEffect.EFFECT_HEAVY_CLICK,
                durationMs = 26L,
                amplitude = 185,
                fallbackCompose = HapticFeedbackType.LongPress
            )
        }
    }

    fun performBoundaryBounce() {
        if (!enabled) return
        val now = System.nanoTime()
        if ((now - lastTickNs) >= 110L * 1_000_000L) {
            lastTickNs = now
            vibrateHardware(
                effectIdApi29 = VibrationEffect.EFFECT_DOUBLE_CLICK,
                durationMs = 18L,
                amplitude = 120,
                fallbackCompose = HapticFeedbackType.TextHandleMove
            )
        }
    }
}

@Composable
fun rememberStudioHaptics(): StudioHapticsController {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val enabled = LocalHapticsEnabled.current
    return remember(context, haptic, enabled) { StudioHapticsController(context, haptic, enabled) }
}
