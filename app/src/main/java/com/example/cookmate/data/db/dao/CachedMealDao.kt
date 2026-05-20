package com.example.cookmate.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cookmate.data.db.entity.CachedMealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedMealDao {

    @Query(
        """
        SELECT * FROM cached_meals
        WHERE strMeal LIKE '%' || :query || '%'
           OR strCategory LIKE '%' || :query || '%'
           OR strArea LIKE '%' || :query || '%'
        ORDER BY lastSyncedAt DESC
        """
    )
    fun searchCachedMeals(query: String): Flow<List<CachedMealEntity>>

    @Query("SELECT * FROM cached_meals WHERE idMeal = :mealId")
    fun observeMeal(mealId: String): Flow<CachedMealEntity?>

    @Query("SELECT * FROM cached_meals WHERE idMeal = :mealId")
    suspend fun getMeal(mealId: String): CachedMealEntity?

    @Query("SELECT * FROM cached_meals WHERE idMeal LIKE 'local-%' ORDER BY lastSyncedAt DESC")
    fun observeLocalMeals(): Flow<List<CachedMealEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeal(meal: CachedMealEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeals(meals: List<CachedMealEntity>)

    @Query("DELETE FROM cached_meals WHERE idMeal = :mealId")
    suspend fun deleteMeal(mealId: String)

    @Query("DELETE FROM cached_meals WHERE lastSyncedAt < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)

    @Query("DELETE FROM cached_meals WHERE lastSyncedAt < :cutoff AND idMeal NOT IN (:protectedMealIds)")
    suspend fun deleteOlderThanExcept(cutoff: Long, protectedMealIds: List<String>)
}
