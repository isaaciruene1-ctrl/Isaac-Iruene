package com.example.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.catalog.WallpaperCatalog
import com.example.data.media.BackdropImageLoader
import com.example.data.media.DownloadProgress
import com.example.data.media.WallpaperMediaService
import com.example.data.media.WallpaperTarget
import com.example.data.model.ColorFamily
import com.example.data.model.DiscoveryCollection
import com.example.data.model.ImageQualityPref
import com.example.data.model.LockScreenSetDoc
import com.example.data.model.ResolutionTier
import com.example.data.model.SearchFilters
import com.example.data.model.SearchSort
import com.example.data.model.ThemeMode
import com.example.data.model.UiStyleMode
import com.example.data.model.UserCollectionDoc
import com.example.data.model.UserPreferencesDoc
import com.example.data.model.Wallpaper
import com.example.data.model.WallpaperCategory
import com.example.data.repository.GuestLocalUserStore
import com.example.data.repository.UserSyncDataSource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SyncState<out T> {
    data object Loading : SyncState<Nothing>
    data class Ready<T>(val data: T) : SyncState<T>
    data class Error(val message: String) : SyncState<Nothing>
}

class BackdropViewModel(
    private val syncRepository: UserSyncDataSource,
    private val mediaService: WallpaperMediaService,
    private val currentUserId: String = GuestLocalUserStore.GUEST_USER_ID
) : ViewModel() {

    val favoriteIds: StateFlow<Set<String>> = syncRepository.observeFavorites(currentUserId)
        .map { docs -> docs.map { it.wallpaperId }.toSet() }
        .catch { emit(emptySet()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptySet())

    val favoriteWallpapers: StateFlow<SyncState<List<Wallpaper>>> = syncRepository.observeFavorites(currentUserId)
        .map<_, SyncState<List<Wallpaper>>> { docs ->
            val items = docs.mapNotNull { WallpaperCatalog.getById(it.wallpaperId) }
            SyncState.Ready(items)
        }
        .catch { err ->
            emit(SyncState.Error(err.localizedMessage ?: "Unable to load favorites"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), SyncState.Loading)

    val collectionsState: StateFlow<SyncState<List<UserCollectionDoc>>> = syncRepository.observeCollections(currentUserId)
        .map<List<UserCollectionDoc>, SyncState<List<UserCollectionDoc>>> { SyncState.Ready(it) }
        .catch { err ->
            emit(SyncState.Error(err.localizedMessage ?: "Unable to load collections"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), SyncState.Loading)

    val lockScreenSetsState: StateFlow<SyncState<List<LockScreenSetDoc>>> = syncRepository.observeLockScreenSets(currentUserId)
        .map<List<LockScreenSetDoc>, SyncState<List<LockScreenSetDoc>>> { SyncState.Ready(it) }
        .catch { err ->
            emit(SyncState.Error(err.localizedMessage ?: "Unable to load lock-screen sets"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), SyncState.Loading)

    val recentlyViewedWallpapers: StateFlow<List<Wallpaper>> = syncRepository.observeRecentlyViewed(currentUserId)
        .map { docs -> docs.mapNotNull { WallpaperCatalog.getById(it.wallpaperId) } }
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val preferences: StateFlow<UserPreferencesDoc> = syncRepository.observePreferences(currentUserId)
        .catch { emit(UserPreferencesDoc(userId = currentUserId)) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            UserPreferencesDoc(userId = currentUserId)
        )

    private val _searchFilters = MutableStateFlow(SearchFilters())
    val searchFilters: StateFlow<SearchFilters> = _searchFilters.asStateFlow()

    val searchResults: StateFlow<List<Wallpaper>> = _searchFilters
        .map { filters -> WallpaperCatalog.search(filters) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), WallpaperCatalog.allWallpapers)

    private val _downloadProgress = MutableStateFlow<DownloadProgress?>(null)
    val downloadProgress: StateFlow<DownloadProgress?> = _downloadProgress.asStateFlow()

    private val _isApplyingWallpaper = MutableStateFlow(false)
    val isApplyingWallpaper: StateFlow<Boolean> = _isApplyingWallpaper.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _cacheBytes = MutableStateFlow(0L)
    val cacheBytes: StateFlow<Long> = _cacheBytes.asStateFlow()

    private val _discoverySeed = MutableStateFlow(System.currentTimeMillis())
    val discoverySeed: StateFlow<Long> = _discoverySeed.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    val favoriteCount: StateFlow<Int> = favoriteIds.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)

    val collectionCount: StateFlow<Int> = collectionsState.map {
        (it as? SyncState.Ready)?.data?.size ?: 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)

    fun toggleFavorite(wallpaperId: String) {
        viewModelScope.launch {
            val isFav = wallpaperId in favoriteIds.value
            val result = if (isFav) {
                syncRepository.removeFavorite(currentUserId, wallpaperId)
            } else {
                syncRepository.addFavorite(currentUserId, wallpaperId)
            }
            if (result.isFailure) {
                _events.tryEmit("Unable to update favorite: ${result.exceptionOrNull()?.localizedMessage ?: "error"}")
            }
        }
    }

    fun recordViewed(wallpaperId: String) {
        viewModelScope.launch {
            syncRepository.recordRecentlyViewed(currentUserId, wallpaperId)
        }
    }

    fun createCollection(name: String, description: String, initialWallpaper: Wallpaper? = null) {
        viewModelScope.launch {
            val res = syncRepository.createCollection(
                userId = currentUserId,
                name = name,
                description = description,
                initialWallpaperIds = listOfNotNull(initialWallpaper?.id),
                coverUrl = initialWallpaper?.thumbnailUrl
            )
            if (res.isSuccess) {
                _events.tryEmit("Created collection \"$name\"")
            } else {
                _events.tryEmit(res.exceptionOrNull()?.localizedMessage ?: "Failed to create collection")
            }
        }
    }

    fun renameCollection(collection: UserCollectionDoc, newName: String, newDescription: String) {
        viewModelScope.launch {
            val res = syncRepository.renameCollection(currentUserId, collection, newName, newDescription)
            if (res.isSuccess) {
                _events.tryEmit("Updated \"$newName\"")
            } else {
                _events.tryEmit(res.exceptionOrNull()?.localizedMessage ?: "Rename failed")
            }
        }
    }

    fun addWallpaperToCollection(collection: UserCollectionDoc, wallpaper: Wallpaper) {
        viewModelScope.launch {
            val res = syncRepository.addWallpaperToCollection(
                userId = currentUserId,
                collection = collection,
                wallpaperId = wallpaper.id,
                coverUrl = wallpaper.thumbnailUrl
            )
            if (res.isSuccess) {
                _events.tryEmit("Saved to ${collection.name}")
            } else {
                _events.tryEmit(res.exceptionOrNull()?.localizedMessage ?: "Failed to save to collection")
            }
        }
    }

    fun removeWallpaperFromCollection(collection: UserCollectionDoc, wallpaperId: String) {
        viewModelScope.launch {
            syncRepository.removeWallpaperFromCollection(currentUserId, collection, wallpaperId)
        }
    }

    fun deleteCollection(collection: UserCollectionDoc) {
        viewModelScope.launch {
            val res = syncRepository.deleteCollection(currentUserId, collection.id)
            if (res.isSuccess) {
                _events.tryEmit("Deleted \"${collection.name}\"")
            }
        }
    }

    fun saveLockScreenSet(set: LockScreenSetDoc) {
        viewModelScope.launch {
            val res = syncRepository.saveLockScreenSet(currentUserId, set)
            if (res.isSuccess) {
                val saved = res.getOrThrow()
                _events.tryEmit("Saved set \"${saved.name}\"")
            } else {
                _events.tryEmit(res.exceptionOrNull()?.localizedMessage ?: "Failed to save set")
            }
        }
    }

    fun toggleLockScreenSetFavorite(setId: String) {
        viewModelScope.launch {
            val res = syncRepository.toggleLockScreenSetFavorite(currentUserId, setId)
            if (res.isFailure) {
                _events.tryEmit(res.exceptionOrNull()?.localizedMessage ?: "Could not bookmark set")
            }
        }
    }

    fun deleteLockScreenSet(set: LockScreenSetDoc) {
        viewModelScope.launch {
            val res = syncRepository.deleteLockScreenSet(currentUserId, set.id)
            if (res.isSuccess) {
                _events.tryEmit("Removed set \"${set.name}\"")
            }
        }
    }

    // --- Search & Filters ---

    fun updateSearchQuery(query: String) {
        _searchFilters.value = _searchFilters.value.copy(query = query)
    }

    fun commitSearchToHistory(query: String) {
        val clean = query.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            syncRepository.updatePreferences(currentUserId) { prefs ->
                val updated = (listOf(clean) + prefs.recentSearches.filterNot {
                    it.equals(clean, ignoreCase = true)
                }).take(15)
                prefs.copy(recentSearches = updated)
            }
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            syncRepository.updatePreferences(currentUserId) { it.copy(recentSearches = emptyList()) }
        }
    }

    fun setResolutionFilter(tier: ResolutionTier?) {
        _searchFilters.value = _searchFilters.value.copy(resolutionTier = tier)
    }

    fun toggleAmoledFilter() {
        _searchFilters.value = _searchFilters.value.copy(amoledOnly = !_searchFilters.value.amoledOnly)
    }

    fun togglePortraitFilter() {
        _searchFilters.value = _searchFilters.value.copy(portraitOnly = !_searchFilters.value.portraitOnly)
    }

    fun toggleDepthStudioFilter() {
        _searchFilters.value = _searchFilters.value.copy(depthStudioOnly = !_searchFilters.value.depthStudioOnly)
    }

    fun setColorFilter(color: ColorFamily?) {
        _searchFilters.value = _searchFilters.value.copy(colorFamily = color)
    }

    fun setCategoryFilter(category: WallpaperCategory?) {
        _searchFilters.value = _searchFilters.value.copy(category = category)
    }

    fun selectCategoryFromHome(category: WallpaperCategory) {
        _searchFilters.value = SearchFilters(
            category = category,
            depthStudioOnly = category == WallpaperCategory.DEPTH_STUDIO
        )
    }

    fun setCollectionFilter(collection: DiscoveryCollection?) {
        _searchFilters.value = _searchFilters.value.copy(collection = collection)
    }

    fun setSearchSort(sort: SearchSort) {
        _searchFilters.value = _searchFilters.value.copy(sort = sort)
    }

    fun resetFilters() {
        _searchFilters.value = SearchFilters()
    }

    // --- Media Actions ---

    fun downloadWallpaper(wallpaper: Wallpaper) {
        viewModelScope.launch {
            mediaService.downloadWallpaper(wallpaper).collect { state ->
                _downloadProgress.value = state
                when (state) {
                    is DownloadProgress.Completed -> {
                        val suffix = if (state.isDuplicateHandled) " (renamed to avoid duplicate)" else ""
                        _events.tryEmit("Saved ${state.fileName} to Pictures/Backdrops$suffix")
                    }
                    is DownloadProgress.Failed -> {
                        _events.tryEmit(state.message)
                    }
                    else -> Unit
                }
            }
        }
    }

    fun clearDownloadStatus() {
        _downloadProgress.value = null
    }

    fun applyWallpaper(
        wallpaper: Wallpaper,
        target: WallpaperTarget,
        allowHomeParallax: Boolean = false
    ) {
        viewModelScope.launch {
            _isApplyingWallpaper.value = true
            val result = mediaService.applyWallpaper(wallpaper, target, allowHomeParallax)
            _isApplyingWallpaper.value = false
            if (result.isSuccess) {
                val label = when (target) {
                    WallpaperTarget.HOME -> "Home screen"
                    WallpaperTarget.LOCK -> "Lock screen"
                    WallpaperTarget.BOTH -> "Home & Lock screens"
                }
                _events.tryEmit("Applied to $label")
            } else {
                _events.tryEmit("Failed to apply wallpaper: ${result.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    fun applyEditedWallpaper(
        wallpaper: Wallpaper,
        target: WallpaperTarget,
        allowHomeParallax: Boolean = false,
        transformBitmap: (Bitmap) -> Bitmap
    ) {
        viewModelScope.launch {
            _isApplyingWallpaper.value = true
            val source = mediaService.loadFullBitmap(wallpaper)
            val result = if (source != null) {
                val edited = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                    transformBitmap(source)
                }
                mediaService.applyBitmapAsWallpaper(edited, target, allowHomeParallax)
            } else {
                Result.failure(IllegalStateException("Unable to decode wallpaper bitmap."))
            }
            _isApplyingWallpaper.value = false
            if (result.isSuccess) {
                val label = when (target) {
                    WallpaperTarget.HOME -> "Home screen"
                    WallpaperTarget.LOCK -> "Lock screen"
                    WallpaperTarget.BOTH -> "Home & Lock screens"
                }
                _events.tryEmit("Applied customized canvas to $label")
            } else {
                _events.tryEmit("Failed to apply wallpaper: ${result.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    fun applyBitmapWallpaper(
        bitmap: Bitmap,
        target: WallpaperTarget,
        allowHomeParallax: Boolean = false
    ) {
        viewModelScope.launch {
            _isApplyingWallpaper.value = true
            val result = mediaService.applyBitmapAsWallpaper(bitmap, target, allowHomeParallax)
            _isApplyingWallpaper.value = false
            if (result.isSuccess) {
                val label = when (target) {
                    WallpaperTarget.HOME -> "Home screen"
                    WallpaperTarget.LOCK -> "Lock screen"
                    WallpaperTarget.BOTH -> "Home & Lock screens"
                }
                _events.tryEmit("Applied to $label")
            } else {
                _events.tryEmit("Failed to apply wallpaper: ${result.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    fun shareWallpaper(wallpaper: Wallpaper) {
        viewModelScope.launch {
            mediaService.shareWallpaper(wallpaper)
        }
    }

    // --- Settings & Cache ---

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            syncRepository.updatePreferences(currentUserId) { it.copy(themeMode = mode.name) }
        }
    }

    fun setAmoledDark(enabled: Boolean) {
        viewModelScope.launch {
            syncRepository.updatePreferences(currentUserId) { it.copy(amoledDark = enabled) }
        }
    }

    fun setImageQuality(quality: ImageQualityPref) {
        viewModelScope.launch {
            syncRepository.updatePreferences(currentUserId) { it.copy(imageQuality = quality.name) }
        }
    }

    fun setWifiOnlyDownloads(enabled: Boolean) {
        viewModelScope.launch {
            syncRepository.updatePreferences(currentUserId) { it.copy(wifiOnlyDownloads = enabled) }
        }
    }

    fun setHomeCategoryChipsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            syncRepository.updatePreferences(currentUserId) { it.copy(homeCategoryChipsEnabled = enabled) }
        }
    }

    fun setUiStyle(style: UiStyleMode) {
        viewModelScope.launch {
            syncRepository.updatePreferences(currentUserId) { it.copy(uiStyle = style.name) }
        }
    }

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        viewModelScope.launch {
            syncRepository.updatePreferences(currentUserId) { it.copy(hapticFeedbackEnabled = enabled) }
        }
    }

    fun setReducedMotionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            syncRepository.updatePreferences(currentUserId) { it.copy(reducedMotionEnabled = enabled) }
        }
    }

    fun setGlassDepthIntensity(intensity: Float) {
        val clamped = intensity.coerceIn(0.55f, 1.45f)
        viewModelScope.launch {
            syncRepository.updatePreferences(currentUserId) { it.copy(glassDepthIntensity = clamped) }
        }
    }

    fun shuffleDiscovery() {
        _discoverySeed.value += 1L
    }

    fun refreshDiscovery(context: Context) {
        viewModelScope.launch {
            _isRefreshing.value = true
            _cacheBytes.value = BackdropImageLoader.getCacheSizeBytes(context)
            _discoverySeed.value += 1L
            kotlinx.coroutines.delay(450)
            _isRefreshing.value = false
        }
    }

    fun refreshCacheSize(context: Context) {
        _cacheBytes.value = BackdropImageLoader.getCacheSizeBytes(context)
    }

    fun clearImageCache(context: Context) {
        viewModelScope.launch {
            BackdropImageLoader.clearCache(context)
            _cacheBytes.value = BackdropImageLoader.getCacheSizeBytes(context)
            _events.tryEmit("Image cache cleared")
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val appContext = context.applicationContext
                BackdropViewModel(
                    syncRepository = GuestLocalUserStore(appContext),
                    mediaService = WallpaperMediaService(appContext),
                    currentUserId = GuestLocalUserStore.GUEST_USER_ID
                )
            }
        }
    }
}
