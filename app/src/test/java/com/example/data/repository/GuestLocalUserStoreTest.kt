package com.example.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ImageQualityPref
import com.example.data.model.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GuestLocalUserStoreTest {

    @Test
    fun guestStore_persistsFavoritesCollectionsAndPreferencesLocally() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = GuestLocalUserStore(context)
        val guestId = GuestLocalUserStore.GUEST_USER_ID

        assertTrue(store.addFavorite(guestId, "nat_01").isSuccess)
        assertTrue(store.observeFavorites(guestId).first().any { it.wallpaperId == "nat_01" })

        val colId = store.createCollection(
            userId = guestId,
            name = "Guest Pastels",
            description = "Local mood board",
            initialWallpaperIds = listOf("abs_01"),
            coverUrl = null
        ).getOrThrow()

        val created = store.observeCollections(guestId).first().first { it.id == colId }
        assertEquals("Guest Pastels", created.name)
        assertEquals(listOf("abs_01"), created.wallpaperIds)

        assertTrue(store.addWallpaperToCollection(guestId, created, "dep_01", null).isSuccess)
        val updated = store.observeCollections(guestId).first().first { it.id == colId }
        assertEquals(listOf("abs_01", "dep_01"), updated.wallpaperIds)

        assertFalse(store.observePreferences(guestId).first().homeCategoryChipsEnabled)

        assertTrue(
            store.updatePreferences(guestId) {
                it.copy(
                    themeMode = ThemeMode.DARK.name,
                    amoledDark = true,
                    imageQuality = ImageQualityPref.HIGH.name,
                    homeCategoryChipsEnabled = true,
                    recentSearches = listOf("pastel")
                )
            }.isSuccess
        )

        // New instance reads persisted SharedPreferences state
        val reloaded = GuestLocalUserStore(context)
        assertTrue(reloaded.observeFavorites(guestId).first().any { it.wallpaperId == "nat_01" })
        assertEquals(ThemeMode.DARK.name, reloaded.observePreferences(guestId).first().themeMode)
        assertTrue(reloaded.observePreferences(guestId).first().amoledDark)
        assertTrue(reloaded.observePreferences(guestId).first().homeCategoryChipsEnabled)
        assertEquals(listOf("pastel"), reloaded.observePreferences(guestId).first().recentSearches)

        assertTrue(reloaded.removeFavorite(guestId, "nat_01").isSuccess)
        assertFalse(reloaded.observeFavorites(guestId).first().any { it.wallpaperId == "nat_01" })
    }
}
