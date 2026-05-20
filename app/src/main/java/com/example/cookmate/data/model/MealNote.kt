package com.example.cookmate.data.model

data class MealNote(
    val mealId: String,
    val noteText: String,
    val rating: Int,
    val updatedAt: Long
)
