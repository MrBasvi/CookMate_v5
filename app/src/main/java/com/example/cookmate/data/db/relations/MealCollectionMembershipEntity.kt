package com.example.cookmate.data.db.relations

data class MealCollectionMembershipEntity(
    val collectionId: Long,
    val title: String,
    val isPinned: Boolean,
    val containsMeal: Boolean
)
