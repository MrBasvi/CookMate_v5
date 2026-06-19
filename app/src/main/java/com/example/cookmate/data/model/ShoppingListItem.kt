package com.example.cookmate.data.model

data class ShoppingListItem(
    val itemId: Long,
    val ingredientName: String,
    val measure: String,
    val quantityCount: Int,
    val isChecked: Boolean,
    val addedAt: Long
)
