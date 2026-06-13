package com.example.cookmate.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recent_meals",
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
data class RecentMealEntity(
    @PrimaryKey
    val mealId: String,
    val viewedAt: Long = System.currentTimeMillis()
)
