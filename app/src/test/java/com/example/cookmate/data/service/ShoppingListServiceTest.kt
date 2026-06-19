package com.example.cookmate.data.service

import com.example.cookmate.data.db.dao.ShoppingListDao
import com.example.cookmate.data.db.entity.ShoppingListItemEntity
import com.example.cookmate.data.model.Ingredient
import com.example.cookmate.data.model.Meal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShoppingListServiceTest {

    @Test
    fun addIngredientsFromMeal_mergesDuplicateUncheckedItems_andCountsDistinctEntries() = runTest {
        val dao = FakeShoppingListDao()
        val service = ShoppingListService(dao)

        val meal = Meal(
            idMeal = "1",
            strMeal = "Pasta",
            strCategory = "Main",
            strArea = "Italian",
            strInstructions = "Cook",
            strMealThumb = "",
            ingredients = listOf(
                Ingredient("Tomato", "2 pcs"),
                Ingredient("Tomato", "2 pcs"),
                Ingredient("Salt", "")
            )
        )

        val affected = service.addIngredientsFromMeal(meal)
        val items = service.observeItems().first()

        assertEquals(2, affected)
        assertEquals(2, items.size)
        assertEquals("Tomato", items.first { it.ingredientName == "Tomato" }.ingredientName)
        assertEquals(2, items.first { it.ingredientName == "Tomato" }.quantityCount)
    }

    @Test
    fun addManualItem_returnsFalseForBlankName() = runTest {
        val service = ShoppingListService(FakeShoppingListDao())

        val added = service.addManualItem("   ", "1 kg")

        assertFalse(added)
        assertTrue(service.observeItems().first().isEmpty())
    }

    @Test
    fun setChecked_andClearCheckedItems_updateStorage() = runTest {
        val dao = FakeShoppingListDao()
        val service = ShoppingListService(dao)

        service.addManualItem("Milk", "1 l")
        val item = service.observeItems().first().single()

        service.setChecked(item, true)
        assertTrue(service.observeItems().first().single().isChecked)

        service.clearCheckedItems()
        assertTrue(service.observeItems().first().isEmpty())
    }

    @Test
    fun clearAllItems_removesWholeList() = runTest {
        val dao = FakeShoppingListDao()
        val service = ShoppingListService(dao)

        service.addManualItem("Milk", "1 l")
        service.addManualItem("Bread", "1 loaf")

        service.clearAllItems()

        assertTrue(service.observeItems().first().isEmpty())
    }

    private class FakeShoppingListDao : ShoppingListDao {
        private val items = MutableStateFlow<List<ShoppingListItemEntity>>(emptyList())
        private var nextId = 1L

        override fun observeItems(): Flow<List<ShoppingListItemEntity>> = items

        override suspend fun findUncheckedItem(
            ingredientName: String,
            measure: String
        ): ShoppingListItemEntity? {
            return items.value.firstOrNull {
                !it.isChecked &&
                    it.ingredientName.equals(ingredientName, ignoreCase = true) &&
                    it.measure.equals(measure, ignoreCase = true)
            }
        }

        override suspend fun insertItem(item: ShoppingListItemEntity): Long {
            val id = nextId++
            items.value = listOf(item.copy(itemId = id)) + items.value
            return id
        }

        override suspend fun updateItem(item: ShoppingListItemEntity) {
            items.value = items.value.map { current -> if (current.itemId == item.itemId) item else current }
        }

        override suspend fun deleteItem(itemId: Long) {
            items.value = items.value.filterNot { it.itemId == itemId }
        }

        override suspend fun deleteCheckedItems() {
            items.value = items.value.filterNot { it.isChecked }
        }

        override suspend fun deleteAllItems() {
            items.value = emptyList()
        }
    }
}
