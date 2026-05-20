package com.example.cookmate.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_meals")
data class RecentMealEntity(
    @PrimaryKey
    val mealId: String,
    val viewedAt: Long = System.currentTimeMillis()
)
