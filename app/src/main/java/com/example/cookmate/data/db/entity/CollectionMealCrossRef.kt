package com.example.cookmate.data.db.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "collection_meals",
    primaryKeys = ["collectionId", "mealId"],
    indices = [Index("mealId")]
)
data class CollectionMealCrossRef(
    val collectionId: Long,
    val mealId: String,
    val addedAt: Long = System.currentTimeMillis()
)
