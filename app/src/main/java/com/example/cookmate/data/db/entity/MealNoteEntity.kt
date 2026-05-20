package com.example.cookmate.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_notes")
data class MealNoteEntity(
    @PrimaryKey
    val mealId: String,
    val noteText: String,
    val rating: Int,
    val updatedAt: Long = System.currentTimeMillis()
)
