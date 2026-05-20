package com.example.cookmate.data.model

data class MealCollectionSummary(
    val collectionId: Long,
    val title: String,
    val description: String,
    val mealCount: Int,
    val isPinned: Boolean,
    val updatedAt: Long
)

data class MealCollectionDetail(
    val collectionId: Long,
    val title: String,
    val description: String,
    val isPinned: Boolean,
    val meals: List<Meal>,
    val updatedAt: Long
)

data class MealCollectionMembership(
    val collectionId: Long,
    val title: String,
    val isPinned: Boolean,
    val containsMeal: Boolean
)
