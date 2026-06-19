package com.example.cookmate.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meal_notes",
    foreignKeys = [
        ForeignKey(
            entity = CachedMealEntity::class,
            parentColumns = ["idMeal"],
            childColumns = ["mealId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("mealId")]
)
data class MealNoteEntity(
    @PrimaryKey
    val mealId: String,
    val noteText: String,
    val rating: Int,
    val updatedAt: Long = System.currentTimeMillis()
)
