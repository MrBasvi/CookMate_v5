package com.example.cookmate.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_list_items")
data class ShoppingListItemEntity(
    @PrimaryKey(autoGenerate = true)
    val itemId: Long = 0,
    val ingredientName: String,
    val measure: String,
    val quantityCount: Int,
    val isChecked: Boolean,
    val addedAt: Long = System.currentTimeMillis()
)
