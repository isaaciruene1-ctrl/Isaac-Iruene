package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.core.firebase.OperationType
import com.example.core.firebase.handleFirestoreError
import com.example.data.model.FavoriteDoc
import com.example.data.model.ImageQualityPref
import com.example.data.model.RecentlyViewedDoc
import com.example.data.model.ThemeMode
import com.example.data.model.UiStyleMode
import com.example.data.model.UserCollectionDoc
import com.example.data.model.UserPreferencesDoc
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class UserSyncRepository(private val db: FirebaseFirestore) : UserSyncDataSource {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun userRoot(userId: String) = db.collection("users").document(userId)

    // --- Favorites ---

    override fun observeFavorites(userId: String): Flow<List<FavoriteDoc>> =
        userRoot(userId).collection("favorites")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    FavoriteDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }

    override suspend fun addFavorite(userId: String, wallpaperId: String): Result<Unit> {
        val docRef = userRoot(userId).collection("favorites").document(wallpaperId)
        val payload = mapOf(
            "userId" to userId,
            "wallpaperId" to wallpaperId,
            "createdAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun removeFavorite(userId: String, wallpaperId: String): Result<Unit> {
        val docRef = userRoot(userId).collection("favorites").document(wallpaperId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Personal Collections ---

    override fun observeCollections(userId: String): Flow<List<UserCollectionDoc>> =
        userRoot(userId).collection("collections")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    UserCollectionDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
            }

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
        val collectionId = "col_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = userRoot(userId).collection("collections").document(collectionId)
        val payload = buildMap<String, Any> {
            put("userId", userId)
            put("name", trimmedName)
            put("description", description.trim().take(240))
            put("wallpaperIds", initialWallpaperIds.distinct().take(150))
            if (!coverUrl.isNullOrBlank()) {
                put("coverUrl", coverUrl.take(500))
            }
            put("createdAt", FieldValue.serverTimestamp())
            put("updatedAt", FieldValue.serverTimestamp())
        }
        return try {
            docRef.set(payload).await()
            Result.success(collectionId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
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
        val docRef = userRoot(userId).collection("collections").document(collection.id)
        val updates = mapOf(
            "name" to trimmedName,
            "description" to newDescription.trim().take(240),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun addWallpaperToCollection(
        userId: String,
        collection: UserCollectionDoc,
        wallpaperId: String,
        coverUrl: String?
    ): Result<Unit> {
        val docRef = userRoot(userId).collection("collections").document(collection.id)
        val updatedIds = (collection.wallpaperIds + wallpaperId).distinct().take(150)
        val updates = buildMap<String, Any> {
            put("wallpaperIds", updatedIds)
            if (collection.coverUrl.isBlank() && !coverUrl.isNullOrBlank()) {
                put("coverUrl", coverUrl.take(500))
            }
            put("updatedAt", FieldValue.serverTimestamp())
        }
        return try {
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun removeWallpaperFromCollection(
        userId: String,
        collection: UserCollectionDoc,
        wallpaperId: String
    ): Result<Unit> {
        val docRef = userRoot(userId).collection("collections").document(collection.id)
        val updatedIds = collection.wallpaperIds.filterNot { it == wallpaperId }
        val updates = mapOf(
            "wallpaperIds" to updatedIds,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    override suspend fun deleteCollection(userId: String, collectionId: String): Result<Unit> {
        val docRef = userRoot(userId).collection("collections").document(collectionId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Recently Viewed ---

    override fun observeRecentlyViewed(userId: String): Flow<List<RecentlyViewedDoc>> =
        userRoot(userId).collection("recentlyViewed")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    RecentlyViewedDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.viewedAt ?: Timestamp(0, 0) }
                    .take(24)
            }

    override suspend fun recordRecentlyViewed(userId: String, wallpaperId: String): Result<Unit> {
        val docRef = userRoot(userId).collection("recentlyViewed").document(wallpaperId)
        return try {
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update("viewedAt", FieldValue.serverTimestamp()).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to userId,
                        "wallpaperId" to wallpaperId,
                        "viewedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    // --- User Preferences ---

    override fun observePreferences(userId: String): Flow<UserPreferencesDoc> =
        userRoot(userId).collection("preferences").document("settings")
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(
                        UserPreferencesDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ) ?: defaultPreferences(userId)
                } else {
                    defaultPreferences(userId)
                }
            }

    override suspend fun updatePreferences(
        userId: String,
        transform: (UserPreferencesDoc) -> UserPreferencesDoc
    ): Result<Unit> {
        val docRef = userRoot(userId).collection("preferences").document("settings")
        return try {
            val snapshot = docRef.get().await()
            val current = if (snapshot.exists()) {
                snapshot.toObject(
                    UserPreferencesDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ) ?: defaultPreferences(userId)
            } else {
                defaultPreferences(userId)
            }
            val next = transform(current)
            if (snapshot.exists()) {
                docRef.update(
                    mapOf(
                        "themeMode" to next.themeMode,
                        "amoledDark" to next.amoledDark,
                        "imageQuality" to next.imageQuality,
                        "wifiOnlyDownloads" to next.wifiOnlyDownloads,
                        "recentSearches" to next.recentSearches.take(15),
                        "homeCategoryChipsEnabled" to next.homeCategoryChipsEnabled,
                        "uiStyle" to next.uiStyle,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to userId,
                        "themeMode" to next.themeMode,
                        "amoledDark" to next.amoledDark,
                        "imageQuality" to next.imageQuality,
                        "wifiOnlyDownloads" to next.wifiOnlyDownloads,
                        "recentSearches" to next.recentSearches.take(15),
                        "homeCategoryChipsEnabled" to next.homeCategoryChipsEnabled,
                        "uiStyle" to next.uiStyle,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    private fun defaultPreferences(userId: String) = UserPreferencesDoc(
        id = "settings",
        userId = userId,
        themeMode = ThemeMode.SYSTEM.name,
        amoledDark = false,
        imageQuality = ImageQualityPref.ORIGINAL.name,
        wifiOnlyDownloads = false,
        recentSearches = emptyList(),
        homeCategoryChipsEnabled = false,
        uiStyle = UiStyleMode.MATERIAL_3.name
    )
}
