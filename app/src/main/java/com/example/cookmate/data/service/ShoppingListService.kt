package com.example.cookmate.data.service

import com.example.cookmate.data.db.dao.ShoppingListDao
import com.example.cookmate.data.db.entity.ShoppingListItemEntity
import com.example.cookmate.data.model.Meal
import com.example.cookmate.data.model.ShoppingListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingListService @Inject constructor(
    private val shoppingListDao: ShoppingListDao
) {

    fun observeItems(): Flow<List<ShoppingListItem>> =
        shoppingListDao.observeItems().map { items -> items.map { it.toModel() } }

    suspend fun addManualItem(name: String, measure: String): Boolean {
        val normalizedName = name.trim()
        if (normalizedName.isEmpty()) return false

        mergeOrInsert(normalizedName, measure.trim())
        return true
    }

    suspend fun addIngredientsFromMeal(meal: Meal): Int {
        var affectedCount = 0
        meal.ingredients
            .map { ingredient -> ingredient.name.trim() to ingredient.measure.trim() }
            .filter { (name, _) -> name.isNotEmpty() }
            .forEach { (name, measure) ->
                mergeOrInsert(name, measure)
                affectedCount++
            }
        return affectedCount
    }

    suspend fun setChecked(item: ShoppingListItem, checked: Boolean) {
        shoppingListDao.updateItem(
            ShoppingListItemEntity(
                itemId = item.itemId,
                ingredientName = item.ingredientName,
                measure = item.measure,
                quantityCount = item.quantityCount,
                isChecked = checked,
                addedAt = item.addedAt
            )
        )
    }

    suspend fun removeItem(itemId: Long) {
        shoppingListDao.deleteItem(itemId)
    }

    suspend fun clearCheckedItems() {
        shoppingListDao.deleteCheckedItems()
    }

    suspend fun clearAllItems() {
        shoppingListDao.deleteAllItems()
    }

    private suspend fun mergeOrInsert(name: String, measure: String) {
        shoppingListDao.mergeUncheckedItem(name, measure)
    }

    private fun ShoppingListItemEntity.toModel() = ShoppingListItem(
        itemId = itemId,
        ingredientName = ingredientName,
        measure = measure,
        quantityCount = quantityCount,
        isChecked = isChecked,
        addedAt = addedAt
    )
}
