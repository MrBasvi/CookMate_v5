package com.example.cookmate.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cookmate.data.db.entity.ShoppingListItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {

    @Query("SELECT * FROM shopping_list_items ORDER BY isChecked ASC, addedAt DESC, ingredientName ASC")
    fun observeItems(): Flow<List<ShoppingListItemEntity>>

    @Query(
        """
        SELECT * FROM shopping_list_items
        WHERE isChecked = 0
          AND lower(ingredientName) = lower(:ingredientName)
          AND lower(measure) = lower(:measure)
        LIMIT 1
        """
    )
    suspend fun findUncheckedItem(ingredientName: String, measure: String): ShoppingListItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingListItemEntity): Long

    @Update
    suspend fun updateItem(item: ShoppingListItemEntity)

    @Query("DELETE FROM shopping_list_items WHERE itemId = :itemId")
    suspend fun deleteItem(itemId: Long)

    @Query("DELETE FROM shopping_list_items WHERE isChecked = 1")
    suspend fun deleteCheckedItems()

    @Query("DELETE FROM shopping_list_items")
    suspend fun deleteAllItems()
}
