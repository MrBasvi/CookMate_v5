package com.example.cookmate.data.db.relations

data class MealCollectionSummaryEntity(
    val collectionId: Long,
    val title: String,
    val description: String,
    val isPinned: Boolean,
    val updatedAt: Long,
    val mealCount: Int
)
