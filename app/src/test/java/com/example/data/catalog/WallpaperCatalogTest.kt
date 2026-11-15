package com.example.data.catalog

import com.example.data.model.ColorFamily
import com.example.data.model.DiscoveryCollection
import com.example.data.model.ResolutionTier
import com.example.data.model.SearchFilters
import com.example.data.model.SearchSort
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WallpaperCatalogTest {

    @Test
    fun catalogHasAtLeast130DistinctWallpapersAndUniqueThumbnails() {
        val all = WallpaperCatalog.allWallpapers
        assertTrue("Expected at least 130 wallpapers, got ${all.size}", all.size >= 130)
        assertEquals("Duplicate IDs detected", all.size, all.map { it.id }.toSet().size)
        assertEquals(
            "Duplicate thumbnail URLs detected",
            all.size,
            all.map { it.thumbnailUrl }.toSet().size
        )
        val depthItems = WallpaperCatalog.getForCollection(DiscoveryCollection.DEPTH_STUDIO)
        assertTrue("Expected at least 18 curated Depth Studio wallpapers, got ${depthItems.size}", depthItems.size >= 18)
        assertTrue(
            "All Depth Studio entries must have portrait orientation for lock-screen clocks",
            depthItems.all { it.height > it.width }
        )
    }

    @Test
    fun all20DiscoveryCollectionsReturnWallpapers() {
        DiscoveryCollection.entries.forEach { collection ->
            val items = WallpaperCatalog.getForCollection(collection)
            assertTrue("Collection $collection should not be empty", items.isNotEmpty())
        }
    }

    @Test
    fun allColorFamiliesHaveMatchesAndSearchFiltersWork() {
        ColorFamily.entries.forEach { family ->
            val results = WallpaperCatalog.search(SearchFilters(colorFamily = family))
            assertTrue("Color family $family returned empty results", results.isNotEmpty())
        }

        val uhdDepth = WallpaperCatalog.search(
            SearchFilters(
                resolutionTier = ResolutionTier.UHD_4K,
                depthStudioOnly = true,
                sort = SearchSort.MOST_POPULAR
            )
        )
        assertTrue(uhdDepth.isNotEmpty())
        assertTrue(uhdDepth.all { it.resolutionTier == ResolutionTier.UHD_4K && it.supportsDepthEffect })
    }

    @Test
    fun resolutionTiersMatchExact4k2kAndFullHdDimensionsInUrls() {
        WallpaperCatalog.allWallpapers.forEach { wp ->
            when (wp.resolutionTier) {
                ResolutionTier.UHD_4K -> {
                    val maxDim = maxOf(wp.width, wp.height)
                    val minDim = minOf(wp.width, wp.height)
                    assertTrue("${wp.id} 4K width/height invalid: ${wp.width}x${wp.height}", maxDim >= 3240 && minDim >= 2160)
                    assertTrue("${wp.id} missing w=${wp.width} in URL", wp.fullResUrl.contains("w=${wp.width}"))
                    assertTrue("${wp.id} missing h=${wp.height} in URL", wp.fullResUrl.contains("h=${wp.height}"))
                }
                ResolutionTier.QHD_1440 -> {
                    val maxDim = maxOf(wp.width, wp.height)
                    val minDim = minOf(wp.width, wp.height)
                    assertTrue("${wp.id} 2K width/height invalid: ${wp.width}x${wp.height}", maxDim >= 2160 && minDim >= 1440)
                    assertTrue("${wp.id} missing w=${wp.width} in URL", wp.fullResUrl.contains("w=${wp.width}"))
                    assertTrue("${wp.id} missing h=${wp.height} in URL", wp.fullResUrl.contains("h=${wp.height}"))
                }
                ResolutionTier.FHD_1080 -> {
                    val maxDim = maxOf(wp.width, wp.height)
                    val minDim = minOf(wp.width, wp.height)
                    assertTrue("${wp.id} FHD width/height invalid: ${wp.width}x${wp.height}", maxDim >= 1620 && minDim >= 1080)
                    assertTrue("${wp.id} missing w=${wp.width} in URL", wp.fullResUrl.contains("w=${wp.width}"))
                    assertTrue("${wp.id} missing h=${wp.height} in URL", wp.fullResUrl.contains("h=${wp.height}"))
                }
            }
        }
    }
}
