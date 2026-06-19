package com.example.cookmate.domain

import com.example.cookmate.data.model.Meal
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class SavedMealsSyncUseCase @Inject constructor(
    private val mealDetailsFetcher: MealDetailsFetcher,
    private val localSyncDataSource: SyncLocalDataSource,
    private val selector: SyncTargetSelector
) {

    suspend fun sync(historyLimit: Int): SavedMealsSyncResult {
        val localMealIds = localSyncDataSource.getLocalMealIds()
        val targetIds = selector.selectTargets(
            favoriteIds = localSyncDataSource.getFavoriteIds(),
            collectionIds = localSyncDataSource.getCollectionMealIds(),
            recentIds = localSyncDataSource.getRecentMealIds(historyLimit),
            historyLimit = historyLimit
        )
        val protectedMealIds = (targetIds + localMealIds).distinct()

        if (targetIds.isEmpty()) {
            localSyncDataSource.clearStaleCache(
                cutoff = System.currentTimeMillis() - CACHE_TTL_MS,
                protectedMealIds = protectedMealIds
            )
            return SavedMealsSyncResult(0, 0, emptyList())
        }

        val failures = mutableListOf<String>()
        var syncedCount = 0

        targetIds.forEach { mealId ->
            try {
                val meal = mealDetailsFetcher.getMealDetails(mealId)
                localSyncDataSource.cacheSyncedMeal(meal)
                syncedCount++
            } catch (throwable: CancellationException) {
                throw throwable
            } catch (_: Exception) {
                failures += mealId
            }
        }

        localSyncDataSource.clearStaleCache(
            cutoff = System.currentTimeMillis() - CACHE_TTL_MS,
            protectedMealIds = protectedMealIds
        )

        return SavedMealsSyncResult(
            requestedCount = targetIds.size,
            syncedCount = syncedCount,
            failedMealIds = failures
        )
    }

    companion object {
        private const val CACHE_TTL_MS = 1000L * 60L * 60L * 24L * 14L
    }
}

data class SavedMealsSyncResult(
    val requestedCount: Int,
    val syncedCount: Int,
    val failedMealIds: List<String>
)

interface MealDetailsFetcher {
    suspend fun getMealDetails(mealId: String): Meal
}

interface SyncLocalDataSource {
    suspend fun getFavoriteIds(): List<String>
    suspend fun getCollectionMealIds(): List<String>
    suspend fun getRecentMealIds(limit: Int): List<String>
    suspend fun getLocalMealIds(): List<String>
    suspend fun cacheSyncedMeal(meal: Meal)
    suspend fun clearStaleCache(cutoff: Long, protectedMealIds: List<String>)
}
