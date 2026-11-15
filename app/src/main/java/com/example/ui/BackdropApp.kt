package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core.auth.authStateFlow
import com.example.core.auth.onGoogleSignInClicked
import com.example.core.auth.signOutUser
import com.example.data.catalog.WallpaperCatalog
import com.example.data.media.WallpaperTarget
import com.example.data.repository.GuestLocalUserStore
import com.example.ui.components.BackdropLogoMark
import com.example.ui.components.BackdropWordmark
import com.example.ui.screens.AiStudioSheet
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CollectionDetailScreen
import com.example.ui.screens.CollectionsScreen
import com.example.ui.screens.DepthStudioScreen
import com.example.ui.screens.ExploreSearchScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PreviewStudioScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.WallpaperDetailScreen
import com.example.ui.theme.BackdropTheme
import com.example.ui.viewmodel.AiStudioViewModel
import com.example.ui.viewmodel.BackdropViewModel
import com.example.ui.viewmodel.DepthStudioViewModel
import com.example.ui.viewmodel.SyncState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay

object BackdropRoutes {
    const val HOME = "home"
    const val EXPLORE = "explore"
    const val FAVORITES = "favorites"
    const val COLLECTIONS = "collections"
    const val PROFILE = "profile"
    const val DETAIL = "detail/{wallpaperId}"
    const val PREVIEW = "preview/{wallpaperId}"
    const val DEPTH = "depth/{wallpaperId}"
    const val COLLECTION_DETAIL = "collection/{collectionId}"

    fun detail(id: String) = "detail/$id"
    fun preview(id: String) = "preview/$id"
    fun depth(id: String) = "depth/$id"
    fun collectionDetail(id: String) = "collection/$id"
}

private enum class TopDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME(BackdropRoutes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    EXPLORE(BackdropRoutes.EXPLORE, "Explore", Icons.Filled.Explore, Icons.Outlined.Explore),
    FAVORITES(BackdropRoutes.FAVORITES, "Favorites", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    COLLECTIONS(BackdropRoutes.COLLECTIONS, "Collections", Icons.Filled.CollectionsBookmark, Icons.Outlined.CollectionsBookmark),
    PROFILE(BackdropRoutes.PROFILE, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun BackdropRootApp() {
    val authFlow = remember { Firebase.auth.authStateFlow() }
    val currentUser by authFlow.collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)
    var showSplash by remember { mutableStateOf(true) }
    var isGuestMode by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(850)
        showSplash = false
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            isGuestMode = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val user = currentUser
        if (user == null && !isGuestMode) {
            BackdropTheme {
                AuthScreen(
                    onAuthSuccess = { isGuestMode = false },
                    onContinueAsGuest = { isGuestMode = true }
                )
            }
        } else if (user != null) {
            key(user.uid) {
                BackdropAuthenticatedShell(
                    user = user,
                    isGuest = false,
                    onExitGuestMode = {}
                )
            }
        } else {
            key("guest_local_session") {
                BackdropAuthenticatedShell(
                    user = null,
                    isGuest = true,
                    onExitGuestMode = { isGuestMode = false }
                )
            }
        }

        AnimatedVisibility(
            visible = showSplash,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            BackdropTheme {
                BackdropSplashOverlay()
            }
        }
    }
}

@Composable
private fun BackdropSplashOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BackdropLogoMark(size = 84.dp)
            BackdropWordmark(markSize = 0.dp, showTagline = true)
        }
    }
}

@Composable
private fun BackdropAuthenticatedShell(
    user: FirebaseUser?,
    isGuest: Boolean,
    onExitGuestMode: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val sessionKey = if (isGuest) GuestLocalUserStore.GUEST_USER_ID else requireNotNull(user).uid

    val backdropViewModel: BackdropViewModel = viewModel(
        key = "backdrop_vm_$sessionKey",
        factory = BackdropViewModel.provideFactory(context, sessionKey, isGuest = isGuest)
    )
    val aiStudioViewModel: AiStudioViewModel = viewModel(
        key = "ai_studio_vm_$sessionKey",
        factory = AiStudioViewModel.provideFactory(context)
    )
    val depthStudioViewModel: DepthStudioViewModel = viewModel(
        key = "depth_studio_vm_$sessionKey",
        factory = DepthStudioViewModel.provideFactory(context)
    )

    val preferences by backdropViewModel.preferences.collectAsStateWithLifecycle()
    val favoriteIds by backdropViewModel.favoriteIds.collectAsStateWithLifecycle()
    val favoriteWallpapers by backdropViewModel.favoriteWallpapers.collectAsStateWithLifecycle()
    val collectionsState by backdropViewModel.collectionsState.collectAsStateWithLifecycle()
    val recentlyViewed by backdropViewModel.recentlyViewedWallpapers.collectAsStateWithLifecycle()
    val searchFilters by backdropViewModel.searchFilters.collectAsStateWithLifecycle()
    val searchResults by backdropViewModel.searchResults.collectAsStateWithLifecycle()
    val downloadProgress by backdropViewModel.downloadProgress.collectAsStateWithLifecycle()
    val isApplying by backdropViewModel.isApplyingWallpaper.collectAsStateWithLifecycle()
    val isRefreshing by backdropViewModel.isRefreshing.collectAsStateWithLifecycle()
    val cacheBytes by backdropViewModel.cacheBytes.collectAsStateWithLifecycle()
    val favoriteCount by backdropViewModel.favoriteCount.collectAsStateWithLifecycle()
    val collectionCount by backdropViewModel.collectionCount.collectAsStateWithLifecycle()
    val depthStudioState by depthStudioViewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(backdropViewModel) {
        backdropViewModel.events.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    var showAiSheet by remember { mutableStateOf(false) }
    var isUpgradingFromGuest by remember { mutableStateOf(false) }
    var snackbarErrorTrigger by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(snackbarErrorTrigger) {
        val msg = snackbarErrorTrigger ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        snackbarErrorTrigger = null
    }

    val triggerGuestGoogleSignIn: () -> Unit = {
        isUpgradingFromGuest = true
        onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                isUpgradingFromGuest = false
            },
            onAuthError = { msg ->
                isUpgradingFromGuest = false
                snackbarErrorTrigger = msg
            },
            scope = scope,
            onAuthCancelled = {
                isUpgradingFromGuest = false
            }
        )
    }

    BackdropTheme(
        themeMode = preferences.parsedThemeMode,
        amoledDark = preferences.amoledDark
    ) {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route ?: BackdropRoutes.HOME

        val isTopLevelRoute = TopDestination.entries.any { it.route == currentRoute }
        val showAiFab = currentRoute == BackdropRoutes.HOME || currentRoute == BackdropRoutes.EXPLORE

        fun navigateTopLevel(route: String) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 840.dp
            val gridColumns = when {
                maxWidth >= 840.dp -> 4
                maxWidth >= 600.dp -> 3
                else -> 2
            }

            Row(modifier = Modifier.fillMaxSize()) {
                if (useRail && isTopLevelRoute) {
                    NavigationRail(
                        modifier = Modifier
                            .fillMaxHeight()
                            .windowInsetsPadding(WindowInsets.statusBars)
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        BackdropLogoMark(size = 32.dp)
                        Spacer(modifier = Modifier.height(20.dp))
                        TopDestination.entries.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = { Text(dest.label) }
                            )
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.weight(1f),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    contentWindowInsets = if (isTopLevelRoute) WindowInsets.statusBars else WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        if (!useRail && isTopLevelRoute) {
                            NavigationBar(
                                tonalElevation = 4.dp,
                                modifier = Modifier.testTag("bottom_navigation_bar")
                            ) {
                                TopDestination.entries.forEach { dest ->
                                    val selected = currentRoute == dest.route
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { navigateTopLevel(dest.route) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                contentDescription = dest.label
                                            )
                                        },
                                        label = { Text(dest.label, maxLines = 1) },
                                        modifier = Modifier.testTag("nav_${dest.route}")
                                    )
                                }
                            }
                        }
                    },
                    floatingActionButton = {
                        if (showAiFab) {
                            ExtendedFloatingActionButton(
                                onClick = { showAiSheet = true },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null
                                    )
                                },
                                text = { Text("AI Studio") },
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.testTag("ai_studio_fab")
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = BackdropRoutes.HOME,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        composable(BackdropRoutes.HOME) {
                            HomeScreen(
                                userDisplayName = if (isGuest) "Guest" else user?.displayName,
                                favoriteIds = favoriteIds,
                                isRefreshing = isRefreshing,
                                onRefresh = { backdropViewModel.refreshDiscovery(context) },
                                onSearchShortcut = { navigateTopLevel(BackdropRoutes.EXPLORE) },
                                onProfileShortcut = { navigateTopLevel(BackdropRoutes.PROFILE) },
                                onOpenWallpaper = { wp -> navController.navigate(BackdropRoutes.detail(wp.id)) },
                                onToggleFavorite = backdropViewModel::toggleFavorite,
                                onOpenCollection = { col ->
                                    backdropViewModel.setCollectionFilter(col)
                                    navigateTopLevel(BackdropRoutes.EXPLORE)
                                },
                                onOpenDepthStudio = { wp -> navController.navigate(BackdropRoutes.depth(wp.id)) }
                            )
                        }

                        composable(BackdropRoutes.EXPLORE) {
                            ExploreSearchScreen(
                                filters = searchFilters,
                                results = searchResults,
                                recentSearches = preferences.recentSearches,
                                favoriteIds = favoriteIds,
                                columns = gridColumns,
                                onQueryChange = backdropViewModel::updateSearchQuery,
                                onCommitSearch = backdropViewModel::commitSearchToHistory,
                                onClearHistory = backdropViewModel::clearSearchHistory,
                                onSelectResolution = backdropViewModel::setResolutionFilter,
                                onToggleAmoled = backdropViewModel::toggleAmoledFilter,
                                onTogglePortrait = backdropViewModel::togglePortraitFilter,
                                onToggleDepthStudio = backdropViewModel::toggleDepthStudioFilter,
                                onSelectColor = backdropViewModel::setColorFilter,
                                onSelectCategory = backdropViewModel::setCategoryFilter,
                                onSelectCollection = backdropViewModel::setCollectionFilter,
                                onSelectSort = backdropViewModel::setSearchSort,
                                onResetFilters = backdropViewModel::resetFilters,
                                onOpenWallpaper = { wp -> navController.navigate(BackdropRoutes.detail(wp.id)) },
                                onToggleFavorite = backdropViewModel::toggleFavorite
                            )
                        }

                        composable(BackdropRoutes.FAVORITES) {
                            FavoritesScreen(
                                favoritesState = favoriteWallpapers,
                                recentlyViewed = recentlyViewed,
                                favoriteIds = favoriteIds,
                                columns = gridColumns,
                                onOpenWallpaper = { wp -> navController.navigate(BackdropRoutes.detail(wp.id)) },
                                onToggleFavorite = backdropViewModel::toggleFavorite,
                                onExploreClick = { navigateTopLevel(BackdropRoutes.EXPLORE) },
                                isGuest = isGuest,
                                onSignInClick = if (isGuest) triggerGuestGoogleSignIn else null
                            )
                        }

                        composable(BackdropRoutes.COLLECTIONS) {
                            CollectionsScreen(
                                collectionsState = collectionsState,
                                onCreateCollection = { name, desc ->
                                    backdropViewModel.createCollection(name, desc, null)
                                },
                                onRenameCollection = backdropViewModel::renameCollection,
                                onDeleteCollection = backdropViewModel::deleteCollection,
                                onOpenCollectionDetail = { col ->
                                    navController.navigate(BackdropRoutes.collectionDetail(col.id))
                                },
                                isGuest = isGuest,
                                onSignInClick = if (isGuest) triggerGuestGoogleSignIn else null
                            )
                        }

                        composable(BackdropRoutes.PROFILE) {
                            ProfileSettingsScreen(
                                displayName = user?.displayName,
                                email = user?.email,
                                favoriteCount = favoriteCount,
                                collectionCount = collectionCount,
                                preferences = preferences,
                                cacheBytes = cacheBytes,
                                onRefreshCacheSize = { backdropViewModel.refreshCacheSize(context) },
                                onClearCache = { backdropViewModel.clearImageCache(context) },
                                onThemeModeChange = backdropViewModel::setThemeMode,
                                onAmoledDarkChange = backdropViewModel::setAmoledDark,
                                onImageQualityChange = backdropViewModel::setImageQuality,
                                onWifiOnlyChange = backdropViewModel::setWifiOnlyDownloads,
                                onSignOut = {
                                    signOutUser(credentialManager = credentialManager, scope = scope)
                                },
                                isGuest = isGuest,
                                isSigningIn = isUpgradingFromGuest,
                                onSignInWithGoogle = triggerGuestGoogleSignIn,
                                onExitGuestMode = onExitGuestMode
                            )
                        }

                        composable(
                            route = BackdropRoutes.DETAIL,
                            arguments = listOf(navArgument("wallpaperId") { type = NavType.StringType })
                        ) { entry ->
                            val wpId = entry.arguments?.getString("wallpaperId").orEmpty()
                            val wp = WallpaperCatalog.getById(wpId) ?: WallpaperCatalog.allWallpapers.first()
                            WallpaperDetailScreen(
                                wallpaper = wp,
                                favoriteIds = favoriteIds,
                                collectionsState = collectionsState,
                                downloadProgress = downloadProgress,
                                isApplying = isApplying,
                                onBack = { navController.popBackStack() },
                                onRecordViewed = backdropViewModel::recordViewed,
                                onToggleFavorite = backdropViewModel::toggleFavorite,
                                onDownload = backdropViewModel::downloadWallpaper,
                                onClearDownloadStatus = backdropViewModel::clearDownloadStatus,
                                onApplyWallpaper = backdropViewModel::applyWallpaper,
                                onShareWallpaper = backdropViewModel::shareWallpaper,
                                onAddToCollection = backdropViewModel::addWallpaperToCollection,
                                onCreateCollectionWithWallpaper = { name, desc, targetWp ->
                                    backdropViewModel.createCollection(name, desc, targetWp)
                                },
                                onOpenPreviewStudio = { targetWp ->
                                    navController.navigate(BackdropRoutes.preview(targetWp.id))
                                },
                                onOpenDepthStudio = { targetWp ->
                                    navController.navigate(BackdropRoutes.depth(targetWp.id))
                                },
                                onOpenAiRemix = { targetWp ->
                                    aiStudioViewModel.prepareRemixFromWallpaper(targetWp)
                                    showAiSheet = true
                                },
                                onOpenAiAnalyze = { targetWp ->
                                    aiStudioViewModel.prepareAnalyzeWallpaper(targetWp)
                                    showAiSheet = true
                                }
                            )
                        }

                        composable(
                            route = BackdropRoutes.PREVIEW,
                            arguments = listOf(navArgument("wallpaperId") { type = NavType.StringType })
                        ) { entry ->
                            val wpId = entry.arguments?.getString("wallpaperId").orEmpty()
                            val wp = WallpaperCatalog.getById(wpId) ?: WallpaperCatalog.allWallpapers.first()
                            PreviewStudioScreen(
                                wallpaper = wp,
                                isApplying = isApplying,
                                onBack = { navController.popBackStack() },
                                onApplyWallpaper = backdropViewModel::applyWallpaper
                            )
                        }

                        composable(
                            route = BackdropRoutes.DEPTH,
                            arguments = listOf(navArgument("wallpaperId") { type = NavType.StringType })
                        ) { entry ->
                            val wpId = entry.arguments?.getString("wallpaperId").orEmpty()
                            val wp = WallpaperCatalog.getById(wpId) ?: WallpaperCatalog.allWallpapers.first()
                            DepthStudioScreen(
                                initialWallpaper = wp,
                                depthState = depthStudioState,
                                isApplying = isApplying,
                                onLoadWallpaper = depthStudioViewModel::loadWallpaperForDepthStudio,
                                onPickCustomPhoto = { uri, forceRescan ->
                                    depthStudioViewModel.loadCustomPhotoForDepthStudio(context, uri, forceRescan)
                                },
                                onBack = { navController.popBackStack() },
                                onApplyWallpaper = backdropViewModel::applyWallpaper,
                                onApplyCustomBitmapWallpaper = backdropViewModel::applyBitmapWallpaper
                            )
                        }

                        composable(
                            route = BackdropRoutes.COLLECTION_DETAIL,
                            arguments = listOf(navArgument("collectionId") { type = NavType.StringType })
                        ) { entry ->
                            val colId = entry.arguments?.getString("collectionId").orEmpty()
                            val cols = (collectionsState as? SyncState.Ready)?.data.orEmpty()
                            val collection = cols.firstOrNull { it.id == colId }
                            if (collection != null) {
                                CollectionDetailScreen(
                                    collection = collection,
                                    favoriteIds = favoriteIds,
                                    columns = gridColumns,
                                    onBack = { navController.popBackStack() },
                                    onOpenWallpaper = { wp -> navController.navigate(BackdropRoutes.detail(wp.id)) },
                                    onToggleFavorite = backdropViewModel::toggleFavorite,
                                    onRemoveFromCollection = { wpId ->
                                        backdropViewModel.removeWallpaperFromCollection(collection, wpId)
                                    }
                                )
                            } else {
                                LaunchedEffect(colId) {
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAiSheet) {
            AiStudioSheet(
                viewModel = aiStudioViewModel,
                onDismiss = { showAiSheet = false }
            )
        }
    }
}
