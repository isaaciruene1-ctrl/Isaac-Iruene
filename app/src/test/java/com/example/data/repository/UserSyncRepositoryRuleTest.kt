package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.ImageQualityPref
import com.example.data.model.ThemeMode
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class UserSyncRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun favorites_authenticatedOwner_canAddObserveAndRemove() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repo = UserSyncRepository(firestore)
        val wpId = "nat_${UUID.randomUUID().toString().take(8)}"

        val addResult = withTimeout(TIMEOUT_MS) { repo.addFavorite(aliceUid, wpId) }
        assertTrue(addResult.isSuccess)

        val favorites = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeFavorites(aliceUid).first { list -> list.any { it.wallpaperId == wpId } }
        }
        assertTrue(favorites.any { it.wallpaperId == wpId })

        val removeResult = withTimeout(TIMEOUT_MS) { repo.removeFavorite(aliceUid, wpId) }
        assertTrue(removeResult.isSuccess)
    }

    @Test
    fun collections_ownerCrudAndCrossUserIsolation_enforced() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = UserSyncRepository(firestore)

        val colId = withTimeout(TIMEOUT_MS) {
            aliceRepo.createCollection(
                userId = aliceUid,
                name = "Dreamy Pastels",
                description = "Calm soft gradient wallpapers",
                initialWallpaperIds = listOf("abs_01"),
                coverUrl = "https://images.unsplash.com/photo-1"
            ).getOrThrow()
        }
        assertTrue(colId.isNotBlank())

        val created = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeCollections(aliceUid).first { list -> list.any { it.id == colId } }
                .first { it.id == colId }
        }
        assertEquals("Dreamy Pastels", created.name)

        val addWp = withTimeout(TIMEOUT_MS) {
            aliceRepo.addWallpaperToCollection(aliceUid, created, "dep_02", null)
        }
        assertTrue(addWp.isSuccess)

        val renamed = withTimeout(TIMEOUT_MS) {
            aliceRepo.renameCollection(aliceUid, created, "Morning Canvas", "Updated notes")
        }
        assertTrue(renamed.isSuccess)

        // Cross-user isolation check: Bob cannot add to Alice's user collection
        signInTestUser(BOB_EMAIL)
        val bobRepo = UserSyncRepository(firestore)
        val crossUserResult = withTimeout(TIMEOUT_MS) {
            bobRepo.deleteCollection(aliceUid, colId)
        }
        assertTrue(crossUserResult.isFailure)
        val firestoreEx = crossUserResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)

        // Clean up as Alice
        signInTestUser(ALICE_EMAIL)
        val deleteRes = withTimeout(TIMEOUT_MS) { aliceRepo.deleteCollection(aliceUid, colId) }
        assertTrue(deleteRes.isSuccess)
    }

    @Test
    fun preferencesAndRecentlyViewed_ownerCanPersistAndUpdate() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repo = UserSyncRepository(firestore)

        val viewRes = withTimeout(TIMEOUT_MS) { repo.recordRecentlyViewed(aliceUid, "amo_01") }
        assertTrue(viewRes.isSuccess)

        val prefRes = withTimeout(TIMEOUT_MS) {
            repo.updatePreferences(aliceUid) { current ->
                current.copy(
                    themeMode = ThemeMode.DARK.name,
                    amoledDark = true,
                    imageQuality = ImageQualityPref.HIGH.name,
                    uiStyle = com.example.data.model.UiStyleMode.FROSTED_GLASS_OS.name,
                    recentSearches = listOf("pastel", "depth")
                )
            }
        }
        assertTrue(prefRes.isSuccess)

        val prefs = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observePreferences(aliceUid).first { it.amoledDark }
        }
        assertEquals(ThemeMode.DARK.name, prefs.themeMode)
        assertEquals(com.example.data.model.UiStyleMode.FROSTED_GLASS_OS.name, prefs.uiStyle)
        assertEquals(listOf("pastel", "depth"), prefs.recentSearches)
    }

    @Test
    fun observeFavorites_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = UserSyncRepository(firestore)

        val exception = try {
            withTimeout(FLOW_TIMEOUT_MS) { repo.observeFavorites("some_uid").first() }
            null
        } catch (e: Exception) {
            e
        }
        assertNotNull(exception)
        val firestoreException = exception?.unwrapFirestoreException()
        assertNotNull(firestoreException)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
        assertFalse(auth.currentUser != null)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_backdrop@test.com"
        const val BOB_EMAIL = "bob_backdrop@test.com"
        const val TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3500L
    }
}
