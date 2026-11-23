package com.example.engine.overlay

import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.R
import com.example.data.catalog.WallpaperCatalog
import com.example.data.model.LockScreenSetDoc
import com.example.data.repository.GuestLocalUserStore
import com.example.data.weather.OpenMeteoWeatherRepository
import com.example.data.weather.WeatherTelemetryState
import com.example.ui.components.ClockDesignConfig
import com.example.ui.components.ClockWidgetTelemetry
import com.example.ui.components.HyperOsLockClock
import com.example.ui.components.WallpaperAsyncImage
import com.example.ui.theme.BackdropTheme

/**
 * Legitimate KLCK-style Lock-Screen Clock Overlay Service.
 *
 * Architecture & Security Guarantees:
 * - Uses official `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY` gated on `Settings.canDrawOverlays`.
 * - Runs under `foregroundServiceType="specialUse"` only while Lock Screen Mode is enabled by the user.
 * - Never suppresses, disables, or bypasses the secure Android PIN/Pattern/Password/Biometric Keyguard:
 *   tapping "Unlock" or receiving `ACTION_USER_PRESENT` immediately removes the overlay so system
 *   authentication runs completely unimpeded.
 */
class LockScreenOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private var overlayLifecycleOwner: OverlayLifecycleOwner? = null
    private var receiverRegistered = false

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> evaluateAndSyncOverlay()
                Intent.ACTION_SCREEN_OFF -> evaluateAndSyncOverlay()
                Intent.ACTION_USER_PRESENT -> removeOverlayView()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as? WindowManager
        ensureNotificationChannel()
        registerScreenLifecycleReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            removeOverlayView()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val store = GuestLocalUserStore(this)
        val prefs = store.getCurrentPreferencesSync()
        if (!prefs.lockScreenOverlayEnabled || !Settings.canDrawOverlays(this)) {
            removeOverlayView()
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = buildStatusNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        if (intent?.action == ACTION_PREVIEW_TEST) {
            attachOverlayView(store)
        } else {
            evaluateAndSyncOverlay()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        removeOverlayView()
        if (receiverRegistered) {
            runCatching { unregisterReceiver(screenReceiver) }
            receiverRegistered = false
        }
        super.onDestroy()
    }

    private fun registerScreenLifecycleReceiver() {
        if (receiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        ContextCompat.registerReceiver(this, screenReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        receiverRegistered = true
    }

    private fun evaluateAndSyncOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            removeOverlayView()
            stopSelf()
            return
        }
        val store = GuestLocalUserStore(this)
        val prefs = store.getCurrentPreferencesSync()
        if (!prefs.lockScreenOverlayEnabled) {
            removeOverlayView()
            stopSelf()
            return
        }
        val kgm = getSystemService(KEYGUARD_SERVICE) as? KeyguardManager
        if (kgm?.isKeyguardLocked == true) {
            attachOverlayView(store)
        } else {
            removeOverlayView()
        }
    }

    private fun attachOverlayView(store: GuestLocalUserStore) {
        if (overlayView != null || !Settings.canDrawOverlays(this)) return
        val wm = windowManager ?: return

        val prefs = store.getCurrentPreferencesSync()
        val sets = store.getLockScreenSetsSync()
        val selectedSet = sets.firstOrNull { it.id == prefs.activeOverlaySetId } ?: sets.firstOrNull()
        val activeClock = selectedSet?.let { ClockDesignConfig.fromJsonString(it.clockConfigJson) }
            ?: store.loadClockDesignConfig()
            ?: ClockDesignConfig()

        val weatherReady = OpenMeteoWeatherRepository.state.value as? WeatherTelemetryState.Ready
        val telemetry = ClockWidgetTelemetry(
            weatherText = weatherReady?.compactReadout ?: "READY",
            batteryPercent = OpenMeteoWeatherRepository.readBatteryPercent(this),
            isWeatherAvailable = weatherReady != null
        )

        val owner = OverlayLifecycleOwner().apply { onCreate() }
        overlayLifecycleOwner = owner

        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setContent {
                BackdropTheme(
                    themeMode = prefs.parsedThemeMode,
                    amoledDark = prefs.amoledDark,
                    uiStyle = prefs.parsedUiStyle,
                    hapticFeedbackEnabled = prefs.hapticFeedbackEnabled,
                    reducedMotionEnabled = prefs.reducedMotionEnabled,
                    glassDepthIntensity = prefs.glassDepthIntensity
                ) {
                    LockScreenOverlaySurface(
                        selectedSet = selectedSet,
                        clockConfig = activeClock,
                        telemetry = telemetry,
                        onDismissToUnlock = { removeOverlayView() }
                    )
                }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        try {
            wm.addView(composeView, params)
            overlayView = composeView
            owner.onResume()
        } catch (e: Exception) {
            Log.e(TAG, "Unable to attach overlay window", e)
            owner.onDestroy()
            overlayLifecycleOwner = null
            overlayView = null
        }
    }

    private fun removeOverlayView() {
        val view = overlayView ?: return
        runCatching { windowManager?.removeViewImmediate(view) }
        overlayLifecycleOwner?.onDestroy()
        overlayLifecycleOwner = null
        overlayView = null
    }

    private fun ensureNotificationChannel() {
        val mgr = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager ?: return
        if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Backdrop Lock Screen Mode",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active while Backdrop custom lock-screen clock overlay is enabled"
                setShowBadge(false)
            }
            mgr.createNotificationChannel(channel)
        }
    }

    private fun buildStatusNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            10,
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this,
            11,
            Intent(this, LockScreenOverlayService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_splash_logo)
            .setContentTitle("Backdrop Lock Screen Active")
            .setContentText("Tap to configure or dismiss before biometric/PIN unlock")
            .setContentIntent(openIntent)
            .addAction(0, "Turn Off", stopIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    companion object {
        private const val TAG = "BackdropOverlaySvc"
        private const val CHANNEL_ID = "backdrop_lockscreen_overlay"
        private const val NOTIFICATION_ID = 4102
        const val ACTION_START = "com.example.engine.overlay.ACTION_START"
        const val ACTION_STOP = "com.example.engine.overlay.ACTION_STOP"
        const val ACTION_PREVIEW_TEST = "com.example.engine.overlay.ACTION_PREVIEW_TEST"

        fun start(context: Context, action: String = ACTION_START) {
            if (!Settings.canDrawOverlays(context)) return
            val intent = Intent(context, LockScreenOverlayService::class.java).setAction(action)
            runCatching {
                ContextCompat.startForegroundService(context, intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, LockScreenOverlayService::class.java).setAction(ACTION_STOP)
            runCatching {
                context.startService(intent)
            }
        }
    }
}

@Composable
internal fun LockScreenOverlaySurface(
    selectedSet: LockScreenSetDoc?,
    clockConfig: ClockDesignConfig,
    telemetry: ClockWidgetTelemetry,
    onDismissToUnlock: () -> Unit
) {
    val wallpaper = selectedSet?.wallpaperId?.let { WallpaperCatalog.getById(it) }
        ?: WallpaperCatalog.allWallpapers.first()
    val primaryColor = clockConfig.customPrimaryArgb?.let { Color(it.toInt()) } ?: Color(0xFFF6F8FC)
    val secondaryColor = clockConfig.customSecondaryArgb?.let { Color(it.toInt()) }
        ?: wallpaper.dominantColors.firstOrNull()?.let { Color(it) }
        ?: MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0C12))
    ) {
        WallpaperAsyncImage(
            wallpaper = wallpaper,
            useFullRes = true,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.28f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.48f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                HyperOsLockClock(
                    face = clockConfig.face,
                    font = clockConfig.font,
                    finish = clockConfig.finish,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    weightLevel = clockConfig.weightLevel,
                    scaleFactor = clockConfig.scale,
                    widthScale = clockConfig.widthScale,
                    heightScale = clockConfig.heightScale,
                    opacity = clockConfig.opacity,
                    alignment = clockConfig.alignment,
                    useLiveTime = true,
                    is24HourFormat = clockConfig.is24HourFormat,
                    showDateAndWeather = clockConfig.showDateAndWeather,
                    widgetsEnabled = clockConfig.widgetsEnabled,
                    showDateWidget = clockConfig.showDateWidget,
                    showWeatherWidget = clockConfig.showWeatherWidget,
                    showBatteryWidget = clockConfig.showBatteryWidget,
                    showDecorativeWidget = clockConfig.showDecorativeWidget,
                    telemetry = telemetry,
                    signatureMode = clockConfig.signatureMode,
                    signatureText = clockConfig.signatureText,
                    liquidGlassConfig = clockConfig.liquidGlass
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.45f)
                ) {
                    Text(
                        text = "System PIN / Biometric Keyguard remains active",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.88f),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
                Button(
                    onClick = onDismissToUnlock,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.92f),
                        contentColor = Color(0xFF10131C)
                    ),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Rounded.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Unlock Device", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private class OverlayLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry

    fun onCreate() {
        savedStateController.performRestore(Bundle())
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    fun onResume() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }
}
