package com.example.cookmate.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cookmate.data.db.entity.MealNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealNoteDao {

    @Query("SELECT * FROM meal_notes WHERE mealId = :mealId")
    fun observeMealNote(mealId: String): Flow<MealNoteEntity?>

    @Query("SELECT * FROM meal_notes")
    fun observeAllNotes(): Flow<List<MealNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: MealNoteEntity)

    @Query("DELETE FROM meal_notes WHERE mealId = :mealId")
    suspend fun deleteNote(mealId: String)
}
