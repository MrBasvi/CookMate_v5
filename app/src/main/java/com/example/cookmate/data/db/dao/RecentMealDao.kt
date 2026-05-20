package com.example.cookmate.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cookmate.data.db.entity.RecentMealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentMealDao {

    @Query(
        """
        SELECT c.*, r.viewedAt AS viewedAt
        FROM recent_meals r
        INNER JOIN cached_meals c ON c.idMeal = r.mealId
        ORDER BY r.viewedAt DESC
        LIMIT :limit
        """
    )
    fun observeRecentMeals(limit: Int): Flow<List<RecentMealProjection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecentMeal(recentMeal: RecentMealEntity)

    @Query("DELETE FROM recent_meals WHERE mealId = :mealId")
    suspend fun deleteRecentMeal(mealId: String)

    @Query("SELECT mealId FROM recent_meals ORDER BY viewedAt DESC LIMIT :limit")
    suspend fun getRecentMealIds(limit: Int): List<String>

    @Query("DELETE FROM recent_meals WHERE mealId NOT IN (SELECT mealId FROM recent_meals ORDER BY viewedAt DESC LIMIT :limit)")
    suspend fun trimToLimit(limit: Int)
}

data class RecentMealProjection(
    val idMeal: String,
    val strMeal: String,
    val strCategory: String,
    val strArea: String,
    val strInstructions: String,
    val strMealThumb: String,
    val ingredients: List<com.example.cookmate.data.model.Ingredient>,
    val cachedAt: Long,
    val lastSyncedAt: Long,
    val viewedAt: Long
)
