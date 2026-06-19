package com.example.cookmate.domain

import javax.inject.Inject

class SyncTargetSelector @Inject constructor() {

    fun selectTargets(
        favoriteIds: List<String>,
        collectionIds: List<String>,
        recentIds: List<String>,
        historyLimit: Int,
        maxTargets: Int = 30
    ): List<String> {
        val deduped = linkedSetOf<String>()
        favoriteIds.filterNot(::isLocalMealId).forEach(deduped::add)
        collectionIds.filterNot(::isLocalMealId).forEach(deduped::add)
        recentIds.take(historyLimit).filterNot(::isLocalMealId).forEach(deduped::add)
        return deduped.take(maxTargets)
    }

    private fun isLocalMealId(mealId: String): Boolean = mealId.startsWith("local-")
}
