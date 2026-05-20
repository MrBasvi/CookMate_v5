package com.example.cookmate.data.service

import com.example.cookmate.data.db.dao.CachedMealDao
import com.example.cookmate.data.db.dao.RecentMealDao
import com.example.cookmate.data.db.dao.RecentMealProjection
import com.example.cookmate.data.db.entity.CachedMealEntity
import com.example.cookmate.data.db.entity.RecentMealEntity
import com.example.cookmate.data.model.Meal
import com.example.cookmate.data.model.RecentMeal
import com.example.cookmate.domain.SyncLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineMealService @Inject constructor(
    private val cachedMealDao: CachedMealDao,
    private val recentMealDao: RecentMealDao,
    private val favouriteMealDao: com.example.cookmate.data.db.dao.FavouriteMealDao
) : SyncLocalDataSource {

    fun searchCachedMeals(query: String): Flow<List<Meal>> =
        cachedMealDao.searchCachedMeals(query).map { meals -> meals.map { it.toMeal() } }

    suspend fun getCachedMeal(mealId: String): Meal? =
        cachedMealDao.getMeal(mealId)?.toMeal()

    fun observeLocalMeals(): Flow<List<Meal>> =
        cachedMealDao.observeLocalMeals().map { meals -> meals.map { it.toMeal() } }

    suspend fun cacheMeal(meal: Meal) {
        cachedMealDao.upsertMeal(meal.toEntity())
    }

    suspend fun cacheMeals(meals: List<Meal>) {
        if (meals.isNotEmpty()) {
            cachedMealDao.upsertMeals(meals.map { it.toEntity() })
        }
    }

    suspend fun deleteCachedMeal(mealId: String) {
        recentMealDao.deleteRecentMeal(mealId)
        cachedMealDao.deleteMeal(mealId)
    }

    suspend fun markViewed(meal: Meal, historyLimit: Int) {
        cacheMeal(meal)
        recentMealDao.upsertRecentMeal(RecentMealEntity(mealId = meal.idMeal))
        recentMealDao.trimToLimit(historyLimit)
    }

    fun observeRecentMeals(limit: Int): Flow<List<RecentMeal>> =
        recentMealDao.observeRecentMeals(limit).map { projections ->
            projections.map { it.toRecentMeal() }
        }

    suspend fun clearStaleMeals(cutoff: Long, protectedMealIds: List<String>) {
        if (protectedMealIds.isEmpty()) {
            cachedMealDao.deleteOlderThan(cutoff)
        } else {
            cachedMealDao.deleteOlderThanExcept(cutoff, protectedMealIds)
        }
    }

    override suspend fun getFavoriteIds(): List<String> =
        favouriteMealDao.getAllFavouriteIds()

    override suspend fun getRecentMealIds(limit: Int): List<String> =
        recentMealDao.getRecentMealIds(limit)

    override suspend fun cacheSyncedMeal(meal: Meal) {
        cacheMeal(meal)
    }

    override suspend fun clearStaleCache(cutoff: Long, protectedMealIds: List<String>) {
        clearStaleMeals(cutoff, protectedMealIds)
    }

    internal fun setCollectionIdProvider(provider: suspend () -> List<String>) {
        collectionIdProvider = provider
    }

    private var collectionIdProvider: (suspend () -> List<String>) = { emptyList() }

    override suspend fun getCollectionMealIds(): List<String> = collectionIdProvider()

    private fun CachedMealEntity.toMeal() = Meal(
        idMeal = idMeal,
        strMeal = strMeal,
        strCategory = strCategory,
        strArea = strArea,
        strInstructions = strInstructions,
        strMealThumb = strMealThumb,
        ingredients = ingredients
    )

    private fun Meal.toEntity() = CachedMealEntity(
        idMeal = idMeal,
        strMeal = strMeal,
        strCategory = strCategory,
        strArea = strArea,
        strInstructions = strInstructions,
        strMealThumb = strMealThumb,
        ingredients = ingredients
    )

    private fun RecentMealProjection.toRecentMeal() = RecentMeal(
        meal = Meal(
            idMeal = idMeal,
            strMeal = strMeal,
            strCategory = strCategory,
            strArea = strArea,
            strInstructions = strInstructions,
            strMealThumb = strMealThumb,
            ingredients = ingredients
        ),
        viewedAt = viewedAt
    )
}
