package com.example.cookmate.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class SyncTargetSelectorTest {

    private val selector = SyncTargetSelector()

    @Test
    fun selectTargets_prioritizesFavoritesThenCollectionsThenRecentWithoutDuplicates() {
        val result = selector.selectTargets(
            favoriteIds = listOf("1", "2"),
            collectionIds = listOf("2", "3"),
            recentIds = listOf("3", "4", "5"),
            historyLimit = 2
        )

        assertEquals(listOf("1", "2", "3", "4"), result)
    }

    @Test
    fun selectTargets_respectsMaxTargets() {
        val result = selector.selectTargets(
            favoriteIds = (1..20).map(Int::toString),
            collectionIds = (21..40).map(Int::toString),
            recentIds = (41..60).map(Int::toString),
            historyLimit = 20,
            maxTargets = 5
        )

        assertEquals(listOf("1", "2", "3", "4", "5"), result)
    }
}
