package com.example.cookmate.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "collection_meals",
    primaryKeys = ["collectionId", "mealId"],
    foreignKeys = [
        ForeignKey(
            entity = MealCollectionEntity::class,
            parentColumns = ["collectionId"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CachedMealEntity::class,
            parentColumns = ["idMeal"],
            childColumns = ["mealId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("collectionId"),
        Index("mealId")
    ]
)
data class CollectionMealCrossRef(
    val collectionId: Long,
    val mealId: String,
    val addedAt: Long = System.currentTimeMillis()
)
