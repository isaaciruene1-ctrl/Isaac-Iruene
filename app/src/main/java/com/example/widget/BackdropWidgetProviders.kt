package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.edit
import com.example.MainActivity
import com.example.R
import com.example.data.catalog.ProceduralWallpaperRenderer
import com.example.data.catalog.WallpaperCatalog
import com.example.data.media.WallpaperMediaService
import com.example.data.media.WallpaperTarget
import com.example.data.repository.GuestLocalUserStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object BackdropWidgetContract {
    const val ACTION_APPLY_NEXT = "com.example.widget.ACTION_APPLY_NEXT"
    const val ACTION_TOGGLE_FAVORITE = "com.example.widget.ACTION_TOGGLE_FAVORITE"
    const val EXTRA_OPEN_STUDIO = "com.example.EXTRA_OPEN_STUDIO"
    const val EXTRA_WALLPAPER_ID = "com.example.EXTRA_WALLPAPER_ID"

    private const val PREFS_WIDGET = "backdrop_widget_state"
    private const val KEY_ACTIVE_WP_ID = "active_widget_wallpaper_id"

    fun getCurrentWallpaperId(context: Context): String {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_WIDGET, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_ACTIVE_WP_ID, null)
        return saved?.takeIf { WallpaperCatalog.getById(it) != null }
            ?: WallpaperCatalog.allWallpapers.firstOrNull()?.id.orEmpty()
    }

    fun setCurrentWallpaperId(context: Context, wallpaperId: String) {
        context.applicationContext.getSharedPreferences(PREFS_WIDGET, Context.MODE_PRIVATE)
            .edit { putString(KEY_ACTIVE_WP_ID, wallpaperId) }
    }

    fun advanceNextWallpaperId(context: Context): String {
        val all = WallpaperCatalog.allWallpapers
        if (all.isEmpty()) return ""
        val currentId = getCurrentWallpaperId(context)
        val idx = all.indexOfFirst { it.id == currentId }
        val next = all[(idx + 1) % all.size]
        setCurrentWallpaperId(context, next.id)
        return next.id
    }

    fun refreshAllWidgets(context: Context) {
        val appContext = context.applicationContext
        val manager = AppWidgetManager.getInstance(appContext)
        listOf(
            BackdropWidgetSmallProvider::class.java,
            BackdropWidgetCardProvider::class.java
        ).forEach { cls ->
            val component = ComponentName(appContext, cls)
            val ids = manager.getAppWidgetIds(component)
            if (ids.isNotEmpty()) {
                val updateIntent = Intent(appContext, cls).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                appContext.sendBroadcast(updateIntent)
            }
        }
    }
}

abstract class BaseBackdropWidgetProvider(
    private val isCardLayout: Boolean
) : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateSingleWidget(context, appWidgetManager, appWidgetId, isCardLayout)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val appContext = context.applicationContext
        when (intent.action) {
            BackdropWidgetContract.ACTION_APPLY_NEXT -> {
                val pending = goAsync()
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        val nextId = BackdropWidgetContract.advanceNextWallpaperId(appContext)
                        val wp = WallpaperCatalog.getById(nextId)
                        if (wp != null) {
                            WallpaperMediaService(appContext).applyWallpaper(wp, WallpaperTarget.BOTH)
                        }
                        BackdropWidgetContract.refreshAllWidgets(appContext)
                    } finally {
                        pending.finish()
                    }
                }
            }

            BackdropWidgetContract.ACTION_TOGGLE_FAVORITE -> {
                val pending = goAsync()
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        val currentId = BackdropWidgetContract.getCurrentWallpaperId(appContext)
                        if (currentId.isNotBlank()) {
                            val store = GuestLocalUserStore(appContext)
                            val favs = store.observeFavorites(GuestLocalUserStore.GUEST_USER_ID)
                            // Toggle using SharedPreferences-backed current favorites synchronously via initial snapshot
                            var alreadyFav = false
                            val favFile = appContext.getSharedPreferences("backdrop_guest_store", Context.MODE_PRIVATE)
                            val rawFavs = favFile.getString("favorites", "[]").orEmpty()
                            alreadyFav = rawFavs.contains("\"$currentId\"")
                            if (alreadyFav) {
                                store.removeFavorite(GuestLocalUserStore.GUEST_USER_ID, currentId)
                            } else {
                                store.addFavorite(GuestLocalUserStore.GUEST_USER_ID, currentId)
                            }
                            @Suppress("UNUSED_VARIABLE")
                            val unused = favs
                        }
                        BackdropWidgetContract.refreshAllWidgets(appContext)
                    } finally {
                        pending.finish()
                    }
                }
            }
        }
    }

    companion object {
        fun updateSingleWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            isCardLayout: Boolean
        ) {
            val layoutRes = if (isCardLayout) R.layout.widget_backdrop_card else R.layout.widget_backdrop_small
            val views = RemoteViews(context.packageName, layoutRes)

            val wpId = BackdropWidgetContract.getCurrentWallpaperId(context)
            val wp = WallpaperCatalog.getById(wpId) ?: WallpaperCatalog.allWallpapers.firstOrNull()

            val favPrefs = context.applicationContext.getSharedPreferences("backdrop_guest_store", Context.MODE_PRIVATE)
            val isFavorite = wp != null && favPrefs.getString("favorites", "[]").orEmpty().contains("\"${wp.id}\"")

            views.setTextViewText(
                R.id.widget_apply_label,
                context.getString(R.string.widget_apply)
            )
            views.setTextViewText(
                R.id.widget_fav_label,
                context.getString(if (isFavorite) R.string.widget_favorited else R.string.widget_favorite)
            )

            if (isCardLayout && wp != null) {
                val previewBmp = ProceduralWallpaperRenderer.getOrRenderPreview(wp, 480, 320)
                views.setImageViewBitmap(R.id.widget_card_artwork, previewBmp)
            }

            val targetClass = if (isCardLayout) {
                BackdropWidgetCardProvider::class.java
            } else {
                BackdropWidgetSmallProvider::class.java
            }

            val applyIntent = Intent(context, targetClass).apply {
                action = BackdropWidgetContract.ACTION_APPLY_NEXT
            }
            val applyPending = PendingIntent.getBroadcast(
                context,
                appWidgetId * 10 + 1,
                applyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_apply, applyPending)

            val favIntent = Intent(context, targetClass).apply {
                action = BackdropWidgetContract.ACTION_TOGGLE_FAVORITE
            }
            val favPending = PendingIntent.getBroadcast(
                context,
                appWidgetId * 10 + 2,
                favIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_favorite, favPending)

            val studioIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(BackdropWidgetContract.EXTRA_OPEN_STUDIO, true)
                putExtra(BackdropWidgetContract.EXTRA_WALLPAPER_ID, wp?.id.orEmpty())
            }
            val studioPending = PendingIntent.getActivity(
                context,
                appWidgetId * 10 + 3,
                studioIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_studio, studioPending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

class BackdropWidgetSmallProvider : BaseBackdropWidgetProvider(isCardLayout = false)

class BackdropWidgetCardProvider : BaseBackdropWidgetProvider(isCardLayout = true)
