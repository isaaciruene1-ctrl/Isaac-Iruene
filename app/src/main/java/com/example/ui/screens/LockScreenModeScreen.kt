package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.LockScreenSetDoc
import com.example.data.model.UserPreferencesDoc
import com.example.data.weather.OpenMeteoWeatherRepository
import com.example.engine.overlay.CompatibilityStatus
import com.example.engine.overlay.LockScreenCompatibilityManager
import com.example.engine.overlay.LockScreenOverlayService
import com.example.engine.overlay.LockScreenOverlaySurface
import com.example.ui.components.ClockDesignConfig
import com.example.ui.components.ClockWidgetTelemetry
import com.example.ui.components.StudioOptionPill
import com.example.ui.components.TactileSurface
import com.example.ui.theme.rememberStudioHaptics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreenModeScreen(
    preferences: UserPreferencesDoc,
    savedSets: List<LockScreenSetDoc>,
    currentClockConfig: ClockDesignConfig,
    onBack: () -> Unit,
    onOpenCompatibility: () -> Unit,
    onSelectOverlaySetId: (String) -> Unit,
    onSetOverlayEnabled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptics = rememberStudioHaptics()

    var diagnostics by remember { mutableStateOf(LockScreenCompatibilityManager.inspectDevice(context)) }
    var hasPostNotif by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    var showFullPreview by remember { mutableStateOf(false) }

    val notifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPostNotif = granted
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                diagnostics = LockScreenCompatibilityManager.inspectDevice(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    hasPostNotif = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val activeSet = savedSets.firstOrNull { it.id == preferences.activeOverlaySetId } ?: savedSets.firstOrNull()
    val previewClock = activeSet?.let { ClockDesignConfig.fromJsonString(it.clockConfigJson) } ?: currentClockConfig

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Lock Screen Mode", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("lockscreen_mode_back")) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(
                    onClick = onOpenCompatibility,
                    modifier = Modifier.testTag("lockscreen_mode_compat_action")
                ) {
                    Icon(Icons.Rounded.Devices, contentDescription = "Compatibility Diagnostics")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxSize()
            ) {
                // Enable / Disable Banner
                item {
                    TactileSurface(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                                    Text(
                                        text = "Enable Backdrop Lock Screen",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (preferences.lockScreenOverlayEnabled && diagnostics.canDrawOverlays) {
                                            "Active • Shows custom clock when locked"
                                        } else {
                                            "Displays custom clock overlay over keyguard"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = preferences.lockScreenOverlayEnabled,
                                    onCheckedChange = { enabled ->
                                        haptics.performConfirm()
                                        if (enabled && !diagnostics.canDrawOverlays) {
                                            context.startActivity(
                                                LockScreenCompatibilityManager.buildOverlayPermissionIntent(context)
                                            )
                                        } else {
                                            onSetOverlayEnabled(enabled)
                                            if (enabled) {
                                                LockScreenOverlayService.start(context)
                                            } else {
                                                LockScreenOverlayService.stop(context)
                                            }
                                        }
                                    },
                                    modifier = Modifier.testTag("lockscreen_enable_switch")
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = { showFullPreview = true },
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("lockscreen_preview_button")
                                ) {
                                    Icon(Icons.Rounded.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Preview", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }

                                if (!preferences.lockScreenOverlayEnabled) {
                                    Button(
                                        onClick = {
                                            haptics.performConfirm()
                                            if (!diagnostics.canDrawOverlays) {
                                                context.startActivity(
                                                    LockScreenCompatibilityManager.buildOverlayPermissionIntent(context)
                                                )
                                            } else {
                                                onSetOverlayEnabled(true)
                                                LockScreenOverlayService.start(context)
                                            }
                                        },
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("lockscreen_apply_button")
                                    ) {
                                        Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Apply Mode", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = {
                                            haptics.performTick(25L)
                                            onSetOverlayEnabled(false)
                                            LockScreenOverlayService.stop(context)
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("lockscreen_disable_button")
                                    ) {
                                        Icon(Icons.Rounded.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Disable", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }

                // Security Guarantee Card
                item {
                    TactileSurface(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Security & Keyguard Coexistence",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Backdrop uses Android's official application overlay layer (like KLCK). Your PIN, pattern, password, and biometric authentication stay completely intact and are never bypassed or captured.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Select Clock Design Set
                item {
                    TactileSurface(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Select Clock Design",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (savedSets.isEmpty()) {
                                Text(
                                    text = "Using your active Depth Studio clock (${currentClockConfig.face.label}). Save Lock-Screen Sets in Depth Studio to switch setups here.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(savedSets, key = { it.id }) { setDoc ->
                                        StudioOptionPill(
                                            label = setDoc.name,
                                            selected = (activeSet?.id == setDoc.id),
                                            onClick = {
                                                haptics.performTick(24L)
                                                onSelectOverlaySetId(setDoc.id)
                                            },
                                            modifier = Modifier.testTag("lockscreen_set_pill_${setDoc.id}")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Required Permissions Checklist
                item {
                    TactileSurface(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Grant Required Permissions",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            PermissionChecklistRow(
                                icon = Icons.Rounded.Layers,
                                title = "Display Over Other Apps",
                                description = "Allows drawing your custom clock layer over the pre-unlock keyguard.",
                                isGranted = diagnostics.canDrawOverlays,
                                actionLabel = if (diagnostics.canDrawOverlays) "Granted" else "Grant",
                                onClick = {
                                    context.startActivity(LockScreenCompatibilityManager.buildOverlayPermissionIntent(context))
                                },
                                rowTag = "perm_overlay_row"
                            )

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                PermissionChecklistRow(
                                    icon = Icons.Rounded.NotificationsActive,
                                    title = "Foreground Status Notification",
                                    description = "Required by Android 14+ to keep lock-screen detection reliable.",
                                    isGranted = hasPostNotif,
                                    actionLabel = if (hasPostNotif) "Granted" else "Allow",
                                    onClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                                    rowTag = "perm_notif_row"
                                )
                            }

                            PermissionChecklistRow(
                                icon = Icons.Rounded.BatteryChargingFull,
                                title = "Unrestricted Battery Usage",
                                description = "Recommended so OEM battery savers do not kill screen-on events.",
                                isGranted = diagnostics.isBatteryOptimizationIgnored,
                                actionLabel = if (diagnostics.isBatteryOptimizationIgnored) "Enabled" else "Configure",
                                onClick = {
                                    runCatching {
                                        context.startActivity(LockScreenCompatibilityManager.buildBatteryOptimizationIntent(context))
                                    }
                                },
                                rowTag = "perm_battery_row"
                            )
                        }
                    }
                }

                // OEM Compatibility summary link
                item {
                    TactileSurface(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .clickable(onClick = onOpenCompatibility)
                            .testTag("open_compat_diagnostics_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${diagnostics.detectedProfile.displayName} • ${diagnostics.effectiveStatus.label}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "View full OEM compatibility & device diagnostics",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = if (diagnostics.effectiveStatus == CompatibilityStatus.FULLY_SUPPORTED) {
                                    Icons.Rounded.CheckCircle
                                } else {
                                    Icons.Rounded.WarningAmber
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }

    if (showFullPreview) {
        Dialog(
            onDismissRequest = { showFullPreview = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val telemetry = ClockWidgetTelemetry(
                weatherText = "READY",
                batteryPercent = OpenMeteoWeatherRepository.readBatteryPercent(context),
                isWeatherAvailable = false
            )
            LockScreenOverlaySurface(
                selectedSet = activeSet,
                clockConfig = previewClock,
                telemetry = telemetry,
                onDismissToUnlock = { showFullPreview = false }
            )
        }
    }
}

@Composable
private fun PermissionChecklistRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    actionLabel: String,
    onClick: () -> Unit,
    rowTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(rowTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f).padding(end = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column {
                Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (isGranted) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = "Granted",
                tint = MaterialTheme.colorScheme.primary
            )
        } else {
            FilledTonalButton(
                onClick = onClick,
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(actionLabel, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
