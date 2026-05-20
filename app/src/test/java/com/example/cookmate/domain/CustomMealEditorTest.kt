package com.example.cookmate.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomMealEditorTest {

    @Test
    fun createMeal_appliesDefaults_parsesIngredients_andKeepsImageUrl() {
        val meal = CustomMealEditor.createMeal(
            idProvider = { "local-1" },
            draft = CustomMealDraft(
                title = "  Домашняя паста  ",
                category = " ",
                area = "",
                instructions = " ",
                ingredientsText = "Мука - 200 г\nЯйца: 2 шт\nСоль",
                imageUrl = " https://example.com/pasta.jpg "
            )
        )

        assertEquals("local-1", meal.idMeal)
        assertEquals("Домашняя паста", meal.strMeal)
        assertEquals("Авторский рецепт", meal.strCategory)
        assertEquals("Моя кухня", meal.strArea)
        assertEquals("Инструкция пока не добавлена.", meal.strInstructions)
        assertEquals("https://example.com/pasta.jpg", meal.strMealThumb)
        assertEquals(3, meal.ingredients.size)
        assertEquals("Мука", meal.ingredients[0].name)
        assertEquals("200 г", meal.ingredients[0].measure)
        assertEquals("Яйца", meal.ingredients[1].name)
        assertEquals("2 шт", meal.ingredients[1].measure)
        assertEquals("Соль", meal.ingredients[2].name)
        assertEquals("", meal.ingredients[2].measure)
    }

    @Test
    fun updateMeal_replacesEditableFields_andNormalizesInput() {
        val existing = CustomMealEditor.createMeal(
            idProvider = { "local-2" },
            draft = CustomMealDraft(
                title = "Старый рецепт",
                category = "Выпечка",
                area = "Россия",
                instructions = "Старая инструкция",
                ingredientsText = "Мука - 100 г",
                imageUrl = ""
            )
        )

        val updated = CustomMealEditor.updateMeal(
            existing = existing,
            draft = CustomMealDraft(
                title = "  Новый рецепт ",
                category = "Завтрак",
                area = "Италия",
                instructions = "Смешать и приготовить",
                ingredientsText = "Тесто - 1 порция\nСоус - 2 ложки",
                imageUrl = "https://example.com/new.jpg"
            )
        )

        assertEquals("local-2", updated.idMeal)
        assertEquals("Новый рецепт", updated.strMeal)
        assertEquals("Завтрак", updated.strCategory)
        assertEquals("Италия", updated.strArea)
        assertEquals("Смешать и приготовить", updated.strInstructions)
        assertEquals("https://example.com/new.jpg", updated.strMealThumb)
        assertEquals(2, updated.ingredients.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun createMeal_throws_whenTitleIsBlank() {
        CustomMealEditor.createMeal(
            idProvider = { "local-3" },
            draft = CustomMealDraft(
                title = "   ",
                category = "",
                area = "",
                instructions = "",
                ingredientsText = "",
                imageUrl = ""
            )
        )
    }

    @Test
    fun parseIngredients_skipsBlankLines() {
        val ingredients = CustomMealEditor.parseIngredients("\n\nМолоко - 250 мл\n \nСахар\n")

        assertEquals(2, ingredients.size)
        assertEquals("Молоко", ingredients.first().name)
        assertEquals("250 мл", ingredients.first().measure)
        assertTrue(ingredients.last().measure.isEmpty())
    }
}
