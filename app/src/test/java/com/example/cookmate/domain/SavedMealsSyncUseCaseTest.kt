package com.example.cookmate.domain

import com.example.cookmate.data.model.Meal
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SavedMealsSyncUseCaseTest {

    @Test
    fun sync_refreshesDistinctSavedMeals_andCachesSuccessfulResults() = runTest {
        val cachedMeals = mutableListOf<Meal>()
        val useCase = SavedMealsSyncUseCase(
            mealDetailsFetcher = object : MealDetailsFetcher {
                override suspend fun getMealDetails(mealId: String): Meal {
                    if (mealId == "3") error("broken")
                    return Meal(
                        idMeal = mealId,
                        strMeal = "Meal $mealId",
                        strCategory = "Test",
                        strArea = "Local",
                        strInstructions = "Cook",
                        strMealThumb = "thumb"
                    )
                }
            },
            localSyncDataSource = object : SyncLocalDataSource {
                override suspend fun getFavoriteIds(): List<String> = listOf("1", "2")
                override suspend fun getCollectionMealIds(): List<String> = listOf("2", "3")
                override suspend fun getRecentMealIds(limit: Int): List<String> = listOf("4", "1")
                override suspend fun cacheSyncedMeal(meal: Meal) {
                    cachedMeals += meal
                }

                override suspend fun clearStaleCache(cutoff: Long, protectedMealIds: List<String>) = Unit
            },
            selector = SyncTargetSelector()
        )

        val result = useCase.sync(historyLimit = 5)

        assertEquals(4, result.requestedCount)
        assertEquals(3, result.syncedCount)
        assertEquals(listOf("3"), result.failedMealIds)
        assertEquals(listOf("1", "2", "4"), cachedMeals.map { it.idMeal })
    }

    @Test
    fun sync_returnsEmptyResult_whenNothingNeedsRefresh() = runTest {
        val useCase = SavedMealsSyncUseCase(
            mealDetailsFetcher = object : MealDetailsFetcher {
                override suspend fun getMealDetails(mealId: String): Meal {
                    error("should not be called")
                }
            },
            localSyncDataSource = object : SyncLocalDataSource {
                override suspend fun getFavoriteIds(): List<String> = emptyList()
                override suspend fun getCollectionMealIds(): List<String> = emptyList()
                override suspend fun getRecentMealIds(limit: Int): List<String> = emptyList()
                override suspend fun cacheSyncedMeal(meal: Meal) = Unit
                override suspend fun clearStaleCache(cutoff: Long, protectedMealIds: List<String>) = Unit
            },
            selector = SyncTargetSelector()
        )

        val result = useCase.sync(historyLimit = 15)

        assertEquals(0, result.requestedCount)
        assertEquals(0, result.syncedCount)
        assertTrue(result.failedMealIds.isEmpty())
    }
}
