package com.example.cookmate.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.cookmate.data.db.entity.CollectionMealCrossRef
import com.example.cookmate.data.db.entity.MealCollectionEntity
import com.example.cookmate.data.db.relations.MealCollectionMembershipEntity
import com.example.cookmate.data.db.relations.MealCollectionSummaryEntity
import com.example.cookmate.data.db.relations.MealCollectionWithMeals
import kotlinx.coroutines.flow.Flow

@Dao
interface MealCollectionDao {

    @Query(
        """
        SELECT c.collectionId, c.title, c.description, c.isPinned, c.updatedAt, COUNT(cm.mealId) AS mealCount
        FROM meal_collections c
        LEFT JOIN collection_meals cm ON c.collectionId = cm.collectionId
        GROUP BY c.collectionId
        ORDER BY c.isPinned DESC, c.updatedAt DESC
        """
    )
    fun observeCollectionSummaries(): Flow<List<MealCollectionSummaryEntity>>

    @Transaction
    @Query("SELECT * FROM meal_collections WHERE collectionId = :collectionId")
    fun observeCollection(collectionId: Long): Flow<MealCollectionWithMeals?>

    @Query(
        """
        SELECT c.collectionId, c.title, c.isPinned,
               EXISTS(
                   SELECT 1
                   FROM collection_meals cm
                   WHERE cm.collectionId = c.collectionId AND cm.mealId = :mealId
               ) AS containsMeal
        FROM meal_collections c
        ORDER BY c.isPinned DESC, c.updatedAt DESC
        """
    )
    fun observeMemberships(mealId: String): Flow<List<MealCollectionMembershipEntity>>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1
            FROM collection_meals
            WHERE collectionId = :collectionId AND mealId = :mealId
        )
        """
    )
    suspend fun isMealInCollection(collectionId: Long, mealId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCollection(collection: MealCollectionEntity): Long

    @Update
    suspend fun updateCollection(collection: MealCollectionEntity)

    @Transaction
    suspend fun upsertCollection(collection: MealCollectionEntity): Long {
        val insertedId = insertCollection(collection)
        return if (insertedId == -1L) {
            updateCollection(collection)
            collection.collectionId
        } else {
            insertedId
        }
    }

    @Query("SELECT * FROM meal_collections WHERE collectionId = :collectionId")
    suspend fun getCollection(collectionId: Long): MealCollectionEntity?

    @Query("DELETE FROM meal_collections WHERE collectionId = :collectionId")
    suspend fun deleteCollection(collectionId: Long)

    @Query("DELETE FROM collection_meals WHERE collectionId = :collectionId")
    suspend fun deleteMealsForCollection(collectionId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMealToCollection(crossRef: CollectionMealCrossRef)

    @Query("DELETE FROM collection_meals WHERE collectionId = :collectionId AND mealId = :mealId")
    suspend fun removeMealFromCollection(collectionId: Long, mealId: String)

    @Query("DELETE FROM collection_meals WHERE mealId = :mealId")
    suspend fun removeMealFromAllCollections(mealId: String)

    @Query("SELECT mealId FROM collection_meals")
    suspend fun getAllCollectionMealIds(): List<String>
}
