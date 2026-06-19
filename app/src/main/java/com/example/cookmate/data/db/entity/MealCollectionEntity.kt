package com.example.cookmate.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_collections")
data class MealCollectionEntity(
    @PrimaryKey(autoGenerate = true)
    val collectionId: Long = 0,
    val title: String,
    val description: String,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
