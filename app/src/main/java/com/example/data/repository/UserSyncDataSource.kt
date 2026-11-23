package com.example.data.repository

import android.content.Context
import androidx.core.content.edit
import com.example.data.model.AppLanguageOption
import com.example.data.model.DockLayoutMode
import com.example.data.model.FavoriteDoc
import com.example.data.model.ImageQualityPref
import com.example.data.model.LockScreenSetDoc
import com.example.data.model.MaterialNavStyle
import com.example.data.model.RecentlyViewedDoc
import com.example.data.model.ThemeMode
import com.example.data.model.UiStyleMode
import com.example.data.model.UserCollectionDoc
import com.example.data.model.UserPreferencesDoc
import com.example.ui.components.ClockDesignConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

interface UserSyncDataSource {
    fun observeFavorites(userId: String): Flow<List<FavoriteDoc>>
    suspend fun addFavorite(userId: String, wallpaperId: String): Result<Unit>
    suspend fun removeFavorite(userId: String, wallpaperId: String): Result<Unit>

    fun observeCollections(userId: String): Flow<List<UserCollectionDoc>>
    suspend fun createCollection(
        userId: String,
        name: String,
        description: String = "",
        initialWallpaperIds: List<String> = emptyList(),
        coverUrl: String? = null
    ): Result<String>

    suspend fun renameCollection(
        userId: String,
        collection: UserCollectionDoc,
        newName: String,
        newDescription: String = collection.description
    ): Result<Unit>

    suspend fun addWallpaperToCollection(
        userId: String,
        collection: UserCollectionDoc,
        wallpaperId: String,
        coverUrl: String?
    ): Result<Unit>

    suspend fun removeWallpaperFromCollection(
        userId: String,
        collection: UserCollectionDoc,
        wallpaperId: String
    ): Result<Unit>

    suspend fun deleteCollection(userId: String, collectionId: String): Result<Unit>

    fun observeRecentlyViewed(userId: String): Flow<List<RecentlyViewedDoc>>
    suspend fun recordRecentlyViewed(userId: String, wallpaperId: String): Result<Unit>

    fun observePreferences(userId: String): Flow<UserPreferencesDoc>
    suspend fun updatePreferences(
        userId: String,
        transform: (UserPreferencesDoc) -> UserPreferencesDoc
    ): Result<Unit>

    fun observeLockScreenSets(userId: String): Flow<List<LockScreenSetDoc>>
    suspend fun saveLockScreenSet(userId: String, set: LockScreenSetDoc): Result<LockScreenSetDoc>
    suspend fun toggleLockScreenSetFavorite(userId: String, setId: String): Result<Unit>
    suspend fun deleteLockScreenSet(userId: String, setId: String): Result<Unit>
}

/**
 * Local-only SharedPreferences + StateFlow store for favorites, collections,
 * history, lock-screen sets, and studio preferences.
 */
class GuestLocalUserStore(context: Context) : UserSyncDataSource {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val favoritesFlow = MutableStateFlow(loadFavorites())
    private val collectionsFlow = MutableStateFlow(loadCollections())
    private val recentlyViewedFlow = MutableStateFlow(loadRecentlyViewed())
    private val preferencesFlow = MutableStateFlow(loadPreferences())
    private val lockScreenSetsFlow = MutableStateFlow(loadLockScreenSets())

    override fun observeFavorites(userId: String): Flow<List<FavoriteDoc>> = favoritesFlow

    override suspend fun addFavorite(userId: String, wallpaperId: String): Result<Unit> {
        val current = favoritesFlow.value.filterNot { it.wallpaperId == wallpaperId }
        val updated = listOf(
            FavoriteDoc(
                id = wallpaperId,
                userId = GUEST_USER_ID,
                wallpaperId = wallpaperId,
                createdAt = System.currentTimeMillis()
            )
        ) + current
        favoritesFlow.value = updated
        saveFavorites(updated)
        return Result.success(Unit)
    }

    override suspend fun removeFavorite(userId: String, wallpaperId: String): Result<Unit> {
        val updated = favoritesFlow.value.filterNot { it.wallpaperId == wallpaperId }
        favoritesFlow.value = updated
        saveFavorites(updated)
        return Result.success(Unit)
    }

    override fun observeCollections(userId: String): Flow<List<UserCollectionDoc>> = collectionsFlow

    override suspend fun createCollection(
        userId: String,
        name: String,
        description: String,
        initialWallpaperIds: List<String>,
        coverUrl: String?
    ): Result<String> {
        val trimmedName = name.trim().take(60)
        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Collection name cannot be empty."))
        }
        val collectionId = "guest_col_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val now = System.currentTimeMillis()
        val created = UserCollectionDoc(
            id = collectionId,
            userId = GUEST_USER_ID,
            name = trimmedName,
            description = description.trim().take(240),
            wallpaperIds = initialWallpaperIds.distinct().take(150),
            coverUrl = coverUrl.orEmpty().take(500),
            createdAt = now,
            updatedAt = now
        )
        val updated = listOf(created) + collectionsFlow.value
        collectionsFlow.value = updated
        saveCollections(updated)
        return Result.success(collectionId)
    }

    override suspend fun renameCollection(
        userId: String,
        collection: UserCollectionDoc,
        newName: String,
        newDescription: String
    ): Result<Unit> {
        val trimmedName = newName.trim().take(60)
        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Collection name cannot be empty."))
        }
        val updated = collectionsFlow.value.map { item ->
            if (item.id == collection.id) {
                item.copy(
                    name = trimmedName,
                    description = newDescription.trim().take(240),
                    updatedAt = System.currentTimeMillis()
                )
            } else item
        }
        collectionsFlow.value = updated
        saveCollections(updated)
        return Result.success(Unit)
    }

    override suspend fun addWallpaperToCollection(
        userId: String,
        collection: UserCollectionDoc,
        wallpaperId: String,
        coverUrl: String?
    ): Result<Unit> {
        val updated = collectionsFlow.value.map { item ->
            if (item.id == collection.id) {
                item.copy(
                    wallpaperIds = (item.wallpaperIds + wallpaperId).distinct().take(150),
                    coverUrl = item.coverUrl.ifBlank { coverUrl.orEmpty().take(500) },
                    updatedAt = System.currentTimeMillis()
                )
            } else item
        }
        collectionsFlow.value = updated
        saveCollections(updated)
        return Result.success(Unit)
    }

    override suspend fun removeWallpaperFromCollection(
        userId: String,
        collection: UserCollectionDoc,
        wallpaperId: String
    ): Result<Unit> {
        val updated = collectionsFlow.value.map { item ->
            if (item.id == collection.id) {
                item.copy(
                    wallpaperIds = item.wallpaperIds.filterNot { it == wallpaperId },
                    updatedAt = System.currentTimeMillis()
                )
            } else item
        }
        collectionsFlow.value = updated
        saveCollections(updated)
        return Result.success(Unit)
    }

    override suspend fun deleteCollection(userId: String, collectionId: String): Result<Unit> {
        val updated = collectionsFlow.value.filterNot { it.id == collectionId }
        collectionsFlow.value = updated
        saveCollections(updated)
        return Result.success(Unit)
    }

    override fun observeRecentlyViewed(userId: String): Flow<List<RecentlyViewedDoc>> = recentlyViewedFlow

    override suspend fun recordRecentlyViewed(userId: String, wallpaperId: String): Result<Unit> {
        val filtered = recentlyViewedFlow.value.filterNot { it.wallpaperId == wallpaperId }
        val updated = (listOf(
            RecentlyViewedDoc(
                id = wallpaperId,
                userId = GUEST_USER_ID,
                wallpaperId = wallpaperId,
                viewedAt = System.currentTimeMillis()
            )
        ) + filtered).take(24)
        recentlyViewedFlow.value = updated
        saveRecentlyViewed(updated)
        return Result.success(Unit)
    }

    override fun observePreferences(userId: String): Flow<UserPreferencesDoc> = preferencesFlow

    override suspend fun updatePreferences(
        userId: String,
        transform: (UserPreferencesDoc) -> UserPreferencesDoc
    ): Result<Unit> {
        val next = transform(preferencesFlow.value).copy(
            id = "settings",
            userId = GUEST_USER_ID,
            updatedAt = System.currentTimeMillis()
        )
        preferencesFlow.value = next
        savePreferences(next)
        return Result.success(Unit)
    }

    override fun observeLockScreenSets(userId: String): Flow<List<LockScreenSetDoc>> = lockScreenSetsFlow

    override suspend fun saveLockScreenSet(userId: String, set: LockScreenSetDoc): Result<LockScreenSetDoc> {
        val trimmedName = set.name.trim().take(60)
        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Set name cannot be empty."))
        }
        if (set.wallpaperId.isBlank() && set.customPhotoUri.isBlank()) {
            return Result.failure(IllegalArgumentException("Wallpaper source missing for set."))
        }
        val now = System.currentTimeMillis()
        val id = set.id.ifBlank { "set_${UUID.randomUUID().toString().replace("-", "").take(12)}" }
        val existing = lockScreenSetsFlow.value.firstOrNull { it.id == id }
        val normalized = set.copy(
            id = id,
            name = trimmedName,
            createdAt = existing?.createdAt ?: if (set.createdAt > 0L) set.createdAt else now,
            updatedAt = now
        )
        val updated = listOf(normalized) + lockScreenSetsFlow.value.filterNot { it.id == id }
        lockScreenSetsFlow.value = updated
        saveLockScreenSets(updated)
        return Result.success(normalized)
    }

    override suspend fun toggleLockScreenSetFavorite(userId: String, setId: String): Result<Unit> {
        val found = lockScreenSetsFlow.value.any { it.id == setId }
        if (!found) {
            return Result.failure(NoSuchElementException("Lock-screen set not found."))
        }
        val now = System.currentTimeMillis()
        val updated = lockScreenSetsFlow.value.map { item ->
            if (item.id == setId) item.copy(isFavorite = !item.isFavorite, updatedAt = now) else item
        }
        lockScreenSetsFlow.value = updated
        saveLockScreenSets(updated)
        return Result.success(Unit)
    }

    override suspend fun deleteLockScreenSet(userId: String, setId: String): Result<Unit> {
        val updated = lockScreenSetsFlow.value.filterNot { it.id == setId }
        lockScreenSetsFlow.value = updated
        saveLockScreenSets(updated)
        return Result.success(Unit)
    }

    fun getCurrentPreferencesSync(): UserPreferencesDoc = preferencesFlow.value

    fun getLockScreenSetsSync(): List<LockScreenSetDoc> = lockScreenSetsFlow.value

    private fun loadFavorites(): List<FavoriteDoc> {
        val raw = prefs.getString(KEY_FAVORITES, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val wpId = arr.optString(i)
                    if (wpId.isNotBlank()) {
                        add(FavoriteDoc(id = wpId, userId = GUEST_USER_ID, wallpaperId = wpId))
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun saveFavorites(list: List<FavoriteDoc>) {
        val arr = JSONArray()
        list.forEach { arr.put(it.wallpaperId) }
        prefs.edit { putString(KEY_FAVORITES, arr.toString()) }
    }

    private fun loadCollections(): List<UserCollectionDoc> {
        val raw = prefs.getString(KEY_COLLECTIONS, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val idsArr = obj.optJSONArray("wallpaperIds")
                    val ids = buildList {
                        if (idsArr != null) {
                            for (j in 0 until idsArr.length()) {
                                val v = idsArr.optString(j)
                                if (v.isNotBlank()) add(v)
                            }
                        }
                    }
                    add(
                        UserCollectionDoc(
                            id = obj.optString("id"),
                            userId = GUEST_USER_ID,
                            name = obj.optString("name"),
                            description = obj.optString("description"),
                            wallpaperIds = ids,
                            coverUrl = obj.optString("coverUrl")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun saveCollections(list: List<UserCollectionDoc>) {
        val arr = JSONArray()
        list.forEach { col ->
            val obj = JSONObject().apply {
                put("id", col.id)
                put("name", col.name)
                put("description", col.description)
                put("coverUrl", col.coverUrl)
                put("wallpaperIds", JSONArray(col.wallpaperIds))
            }
            arr.put(obj)
        }
        prefs.edit { putString(KEY_COLLECTIONS, arr.toString()) }
    }

    private fun loadRecentlyViewed(): List<RecentlyViewedDoc> {
        val raw = prefs.getString(KEY_RECENTLY_VIEWED, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val wpId = arr.optString(i)
                    if (wpId.isNotBlank()) {
                        add(RecentlyViewedDoc(id = wpId, userId = GUEST_USER_ID, wallpaperId = wpId))
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun saveRecentlyViewed(list: List<RecentlyViewedDoc>) {
        val arr = JSONArray()
        list.forEach { arr.put(it.wallpaperId) }
        prefs.edit { putString(KEY_RECENTLY_VIEWED, arr.toString()) }
    }

    private fun loadPreferences(): UserPreferencesDoc {
        val searches = runCatching {
            val arr = JSONArray(prefs.getString(KEY_SEARCHES, "[]") ?: "[]")
            buildList {
                for (i in 0 until arr.length()) {
                    val s = arr.optString(i)
                    if (s.isNotBlank()) add(s)
                }
            }
        }.getOrDefault(emptyList())

        return UserPreferencesDoc(
            id = "settings",
            userId = GUEST_USER_ID,
            themeMode = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name,
            amoledDark = prefs.getBoolean(KEY_AMOLED_DARK, false),
            imageQuality = prefs.getString(KEY_IMAGE_QUALITY, ImageQualityPref.ORIGINAL.name)
                ?: ImageQualityPref.ORIGINAL.name,
            wifiOnlyDownloads = prefs.getBoolean(KEY_WIFI_ONLY, false),
            recentSearches = searches,
            homeCategoryChipsEnabled = prefs.getBoolean(KEY_HOME_CATEGORY_CHIPS, false),
            uiStyle = prefs.getString(KEY_UI_STYLE, UiStyleMode.MATERIAL_3.name)
                ?: UiStyleMode.MATERIAL_3.name,
            hapticFeedbackEnabled = prefs.getBoolean(KEY_HAPTICS_ENABLED, true),
            reducedMotionEnabled = prefs.getBoolean(KEY_REDUCED_MOTION, false),
            glassDepthIntensity = prefs.getFloat(KEY_GLASS_DEPTH, 1.0f).coerceIn(0.55f, 1.45f),
            languageCode = prefs.getString(KEY_LANGUAGE_CODE, AppLanguageOption.ENGLISH.code)
                ?: AppLanguageOption.ENGLISH.code,
            onboardingCompleted = prefs.getBoolean(KEY_ONBOARDING_DONE, false),
            dynamicThemingEnabled = prefs.getBoolean(KEY_DYNAMIC_THEMING, true),
            customAccentHex = prefs.getLong(KEY_CUSTOM_ACCENT_HEX, 0xFF6B57A8L),
            m3SurfaceDepth = prefs.getFloat(KEY_M3_SURFACE_DEPTH, 1.0f).coerceIn(0.6f, 1.4f),
            m3CornerRadiusScale = prefs.getFloat(KEY_M3_CORNER_RADIUS, 1.0f).coerceIn(0.5f, 1.5f),
            m3ElevationScale = prefs.getFloat(KEY_M3_ELEVATION, 1.0f).coerceIn(0f, 1.6f),
            m3DensityScale = prefs.getFloat(KEY_M3_DENSITY, 1.0f).coerceIn(0.85f, 1.25f),
            m3NavStyle = prefs.getString(KEY_M3_NAV_STYLE, MaterialNavStyle.PILL_TONAL.name)
                ?: MaterialNavStyle.PILL_TONAL.name,
            m3TypographyScale = prefs.getFloat(KEY_M3_TYPO_SCALE, 1.0f).coerceIn(0.85f, 1.25f),
            m3MotionSpeed = prefs.getFloat(KEY_M3_MOTION_SPEED, 1.0f).coerceIn(0.6f, 1.5f),
            glassBlurLevel = prefs.getFloat(KEY_GLASS_BLUR, 1.0f).coerceIn(0.4f, 1.5f),
            glassRefractionLevel = prefs.getFloat(KEY_GLASS_REFRACTION, 1.0f).coerceIn(0.4f, 1.5f),
            glassTransparencyLevel = prefs.getFloat(KEY_GLASS_TRANSPARENCY, 1.0f).coerceIn(0.5f, 1.4f),
            glassLightResponse = prefs.getFloat(KEY_GLASS_LIGHT_RESP, 1.0f).coerceIn(0.4f, 1.5f),
            glassSpecularHighlights = prefs.getFloat(KEY_GLASS_SPECULAR, 1.0f).coerceIn(0.3f, 1.5f),
            glassBorderSoftness = prefs.getFloat(KEY_GLASS_BORDER_SOFT, 1.0f).coerceIn(0.4f, 1.5f),
            glassAdaptiveContrast = prefs.getFloat(KEY_GLASS_CONTRAST, 1.0f).coerceIn(0.7f, 1.5f),
            dockLayoutMode = prefs.getString(KEY_DOCK_LAYOUT, DockLayoutMode.AUTOMATIC.name)
                ?: DockLayoutMode.AUTOMATIC.name,
            useClassicClockHandles = prefs.getBoolean(KEY_CLASSIC_HANDLES, false),
            weatherCity = prefs.getString(KEY_WEATHER_CITY, "") ?: "",
            weatherUseLocation = prefs.getBoolean(KEY_WEATHER_USE_LOC, false),
            lockScreenOverlayEnabled = prefs.getBoolean(KEY_OVERLAY_ENABLED, false),
            activeOverlaySetId = prefs.getString(KEY_OVERLAY_SET_ID, "") ?: ""
        )
    }

    private fun savePreferences(doc: UserPreferencesDoc) {
        prefs.edit {
            putString(KEY_THEME_MODE, doc.themeMode)
            putBoolean(KEY_AMOLED_DARK, doc.amoledDark)
            putString(KEY_IMAGE_QUALITY, doc.imageQuality)
            putBoolean(KEY_WIFI_ONLY, doc.wifiOnlyDownloads)
            putString(KEY_SEARCHES, JSONArray(doc.recentSearches.take(15)).toString())
            putBoolean(KEY_HOME_CATEGORY_CHIPS, doc.homeCategoryChipsEnabled)
            putString(KEY_UI_STYLE, doc.uiStyle)
            putBoolean(KEY_HAPTICS_ENABLED, doc.hapticFeedbackEnabled)
            putBoolean(KEY_REDUCED_MOTION, doc.reducedMotionEnabled)
            putFloat(KEY_GLASS_DEPTH, doc.glassDepthIntensity.coerceIn(0.55f, 1.45f))
            putString(KEY_LANGUAGE_CODE, doc.languageCode)
            putBoolean(KEY_ONBOARDING_DONE, doc.onboardingCompleted)
            putBoolean(KEY_DYNAMIC_THEMING, doc.dynamicThemingEnabled)
            putLong(KEY_CUSTOM_ACCENT_HEX, doc.customAccentHex)
            putFloat(KEY_M3_SURFACE_DEPTH, doc.m3SurfaceDepth)
            putFloat(KEY_M3_CORNER_RADIUS, doc.m3CornerRadiusScale)
            putFloat(KEY_M3_ELEVATION, doc.m3ElevationScale)
            putFloat(KEY_M3_DENSITY, doc.m3DensityScale)
            putString(KEY_M3_NAV_STYLE, doc.m3NavStyle)
            putFloat(KEY_M3_TYPO_SCALE, doc.m3TypographyScale)
            putFloat(KEY_M3_MOTION_SPEED, doc.m3MotionSpeed)
            putFloat(KEY_GLASS_BLUR, doc.glassBlurLevel)
            putFloat(KEY_GLASS_REFRACTION, doc.glassRefractionLevel)
            putFloat(KEY_GLASS_TRANSPARENCY, doc.glassTransparencyLevel)
            putFloat(KEY_GLASS_LIGHT_RESP, doc.glassLightResponse)
            putFloat(KEY_GLASS_SPECULAR, doc.glassSpecularHighlights)
            putFloat(KEY_GLASS_BORDER_SOFT, doc.glassBorderSoftness)
            putFloat(KEY_GLASS_CONTRAST, doc.glassAdaptiveContrast)
            putString(KEY_DOCK_LAYOUT, doc.dockLayoutMode)
            putBoolean(KEY_CLASSIC_HANDLES, doc.useClassicClockHandles)
            putString(KEY_WEATHER_CITY, doc.weatherCity)
            putBoolean(KEY_WEATHER_USE_LOC, doc.weatherUseLocation)
            putBoolean(KEY_OVERLAY_ENABLED, doc.lockScreenOverlayEnabled)
            putString(KEY_OVERLAY_SET_ID, doc.activeOverlaySetId)
        }
    }

    private fun loadLockScreenSets(): List<LockScreenSetDoc> {
        val raw = prefs.getString(KEY_LOCK_SCREEN_SETS, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val id = obj.optString("id")
                    val name = obj.optString("name")
                    if (id.isNotBlank() && name.isNotBlank()) {
                        add(
                            LockScreenSetDoc(
                                id = id,
                                name = name,
                                wallpaperId = obj.optString("wallpaperId"),
                                customPhotoUri = obj.optString("customPhotoUri"),
                                clockConfigJson = obj.optString("clockConfigJson"),
                                zoomScale = obj.optDouble("zoomScale", 1.0).toFloat().coerceIn(1f, 4f),
                                panOffsetX = obj.optDouble("panOffsetX", 0.0).toFloat(),
                                panOffsetY = obj.optDouble("panOffsetY", 0.0).toFloat(),
                                rotationDegrees = obj.optDouble("rotationDegrees", 0.0).toFloat(),
                                dimFraction = obj.optDouble("dimFraction", 0.0).toFloat().coerceIn(0f, 0.5f),
                                lookFilterName = obj.optString("lookFilterName", "ORIGINAL"),
                                isFavorite = obj.optBoolean("isFavorite", false),
                                createdAt = obj.optLong("createdAt", 0L),
                                updatedAt = obj.optLong("updatedAt", 0L)
                            )
                        )
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun saveLockScreenSets(list: List<LockScreenSetDoc>) {
        val arr = JSONArray()
        list.take(60).forEach { set ->
            arr.put(
                JSONObject().apply {
                    put("id", set.id)
                    put("name", set.name)
                    put("wallpaperId", set.wallpaperId)
                    put("customPhotoUri", set.customPhotoUri)
                    put("clockConfigJson", set.clockConfigJson)
                    put("zoomScale", set.zoomScale.toDouble())
                    put("panOffsetX", set.panOffsetX.toDouble())
                    put("panOffsetY", set.panOffsetY.toDouble())
                    put("rotationDegrees", set.rotationDegrees.toDouble())
                    put("dimFraction", set.dimFraction.toDouble())
                    put("lookFilterName", set.lookFilterName)
                    put("isFavorite", set.isFavorite)
                    put("createdAt", set.createdAt)
                    put("updatedAt", set.updatedAt)
                }
            )
        }
        prefs.edit { putString(KEY_LOCK_SCREEN_SETS, arr.toString()) }
    }

    fun loadClockDesignConfig(): ClockDesignConfig? =
        ClockDesignConfig.fromJsonString(prefs.getString(KEY_DEPTH_CLOCK_CONFIG, null))

    fun saveClockDesignConfig(config: ClockDesignConfig) {
        prefs.edit {
            putString(KEY_DEPTH_CLOCK_CONFIG, config.toJsonString())
        }
    }

    companion object {
        const val GUEST_USER_ID = "guest_local"
        private const val PREFS_NAME = "backdrop_guest_store"
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_COLLECTIONS = "collections"
        private const val KEY_RECENTLY_VIEWED = "recently_viewed"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_AMOLED_DARK = "amoled_dark"
        private const val KEY_IMAGE_QUALITY = "image_quality"
        private const val KEY_WIFI_ONLY = "wifi_only"
        private const val KEY_SEARCHES = "recent_searches"
        private const val KEY_HOME_CATEGORY_CHIPS = "home_category_chips_enabled"
        private const val KEY_UI_STYLE = "ui_style"
        private const val KEY_HAPTICS_ENABLED = "haptic_feedback_enabled"
        private const val KEY_REDUCED_MOTION = "reduced_motion_enabled"
        private const val KEY_GLASS_DEPTH = "glass_depth_intensity"
        private const val KEY_LOCK_SCREEN_SETS = "lock_screen_sets_json"
        private const val KEY_DEPTH_CLOCK_CONFIG = "depth_clock_design_json"
        private const val KEY_LANGUAGE_CODE = "app_language_code"
        private const val KEY_ONBOARDING_DONE = "onboarding_completed"
        private const val KEY_DYNAMIC_THEMING = "dynamic_theming_enabled"
        private const val KEY_CUSTOM_ACCENT_HEX = "custom_accent_hex"
        private const val KEY_M3_SURFACE_DEPTH = "m3_surface_depth"
        private const val KEY_M3_CORNER_RADIUS = "m3_corner_radius"
        private const val KEY_M3_ELEVATION = "m3_elevation"
        private const val KEY_M3_DENSITY = "m3_density"
        private const val KEY_M3_NAV_STYLE = "m3_nav_style"
        private const val KEY_M3_TYPO_SCALE = "m3_typo_scale"
        private const val KEY_M3_MOTION_SPEED = "m3_motion_speed"
        private const val KEY_GLASS_BLUR = "glass_blur"
        private const val KEY_GLASS_REFRACTION = "glass_refraction"
        private const val KEY_GLASS_TRANSPARENCY = "glass_transparency"
        private const val KEY_GLASS_LIGHT_RESP = "glass_light_resp"
        private const val KEY_GLASS_SPECULAR = "glass_specular"
        private const val KEY_GLASS_BORDER_SOFT = "glass_border_soft"
        private const val KEY_GLASS_CONTRAST = "glass_contrast"
        private const val KEY_DOCK_LAYOUT = "dock_layout_mode"
        private const val KEY_CLASSIC_HANDLES = "use_classic_clock_handles"
        private const val KEY_WEATHER_CITY = "weather_city"
        private const val KEY_WEATHER_USE_LOC = "weather_use_location"
        private const val KEY_OVERLAY_ENABLED = "lock_screen_overlay_enabled"
        private const val KEY_OVERLAY_SET_ID = "active_overlay_set_id"
    }
}
