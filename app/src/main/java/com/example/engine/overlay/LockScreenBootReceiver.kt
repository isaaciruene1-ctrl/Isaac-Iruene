package com.example.engine.overlay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.data.repository.GuestLocalUserStore

/**
 * Restores Backdrop's Lock Screen Mode overlay service across reboots or app updates
 * ONLY when explicitly enabled by the user and SYSTEM_ALERT_WINDOW permission remains granted.
 */
class LockScreenBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        val prefs = GuestLocalUserStore(context.applicationContext).getCurrentPreferencesSync()
        if (prefs.lockScreenOverlayEnabled && Settings.canDrawOverlays(context)) {
            LockScreenOverlayService.start(context.applicationContext)
        }
    }
}
