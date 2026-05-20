package com.example.cookmate.data.service

import com.example.cookmate.data.db.dao.MealNoteDao
import com.example.cookmate.data.db.entity.MealNoteEntity
import com.example.cookmate.data.model.MealNote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealNoteService @Inject constructor(
    private val mealNoteDao: MealNoteDao
) {

    fun observeMealNote(mealId: String): Flow<MealNote?> =
        mealNoteDao.observeMealNote(mealId).map { entity -> entity?.toModel() }

    fun observeAllNotes(): Flow<Map<String, MealNote>> =
        mealNoteDao.observeAllNotes().map { notes ->
            notes.associate { it.mealId to it.toModel() }
        }

    suspend fun saveNote(mealId: String, noteText: String, rating: Int) {
        val normalizedRating = rating.coerceIn(0, 5)
        val trimmedNote = noteText.trim()

        if (trimmedNote.isEmpty() && normalizedRating == 0) {
            mealNoteDao.deleteNote(mealId)
            return
        }

        mealNoteDao.upsertNote(
            MealNoteEntity(
                mealId = mealId,
                noteText = trimmedNote,
                rating = normalizedRating
            )
        )
    }

    suspend fun deleteNote(mealId: String) {
        mealNoteDao.deleteNote(mealId)
    }

    private fun MealNoteEntity.toModel() = MealNote(
        mealId = mealId,
        noteText = noteText,
        rating = rating,
        updatedAt = updatedAt
    )
}
