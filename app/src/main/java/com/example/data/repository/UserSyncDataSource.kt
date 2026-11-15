package com.example.data.repository

import android.content.Context
import com.example.data.model.FavoriteDoc
import com.example.data.model.ImageQualityPref
import com.example.data.model.RecentlyViewedDoc
import com.example.data.model.ThemeMode
import com.example.data.model.UserCollectionDoc
import com.example.data.model.UserPreferencesDoc
import com.google.firebase.Timestamp
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
}

/**
 * Local-only SharedPreferences + StateFlow store used exclusively in Guest Mode.
 * Never makes any Firebase Firestore calls.
 */
class GuestLocalUserStore(context: Context) : UserSyncDataSource {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val favoritesFlow = MutableStateFlow(loadFavorites())
    private val collectionsFlow = MutableStateFlow(loadCollections())
    private val recentlyViewedFlow = MutableStateFlow(loadRecentlyViewed())
    private val preferencesFlow = MutableStateFlow(loadPreferences())

    override fun observeFavorites(userId: String): Flow<List<FavoriteDoc>> = favoritesFlow

    override suspend fun addFavorite(userId: String, wallpaperId: String): Result<Unit> {
        val current = favoritesFlow.value.filterNot { it.wallpaperId == wallpaperId }
        val updated = listOf(
            FavoriteDoc(
                id = wallpaperId,
                userId = GUEST_USER_ID,
                wallpaperId = wallpaperId,
                createdAt = Timestamp.now()
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
        val now = Timestamp.now()
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
                    updatedAt = Timestamp.now()
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
                    updatedAt = Timestamp.now()
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
                    updatedAt = Timestamp.now()
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
                viewedAt = Timestamp.now()
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
            updatedAt = Timestamp.now()
        )
        preferencesFlow.value = next
        savePreferences(next)
        return Result.success(Unit)
    }

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
        prefs.edit().putString(KEY_FAVORITES, arr.toString()).apply()
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
        prefs.edit().putString(KEY_COLLECTIONS, arr.toString()).apply()
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
        prefs.edit().putString(KEY_RECENTLY_VIEWED, arr.toString()).apply()
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
            recentSearches = searches
        )
    }

    private fun savePreferences(doc: UserPreferencesDoc) {
        prefs.edit()
            .putString(KEY_THEME_MODE, doc.themeMode)
            .putBoolean(KEY_AMOLED_DARK, doc.amoledDark)
            .putString(KEY_IMAGE_QUALITY, doc.imageQuality)
            .putBoolean(KEY_WIFI_ONLY, doc.wifiOnlyDownloads)
            .putString(KEY_SEARCHES, JSONArray(doc.recentSearches.take(15)).toString())
            .apply()
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
    }
}
