package com.example.cookmate.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.cookmate.data.model.Ingredient

@Entity(tableName = "cached_meals")
data class CachedMealEntity(
    @PrimaryKey
    val idMeal: String,
    val strMeal: String,
    val strCategory: String,
    val strArea: String,
    val strInstructions: String,
    val strMealThumb: String,
    val ingredients: List<Ingredient> = emptyList(),
    val cachedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long = System.currentTimeMillis()
)
