package com.example.cookmate.domain

import com.example.cookmate.data.model.Ingredient
import com.example.cookmate.data.model.Meal

data class CustomMealDraft(
    val title: String,
    val category: String,
    val area: String,
    val instructions: String,
    val ingredientsText: String,
    val imageUrl: String
)

object CustomMealEditor {

    fun createMeal(idProvider: () -> String, draft: CustomMealDraft): Meal {
        val normalizedTitle = draft.title.trim()
        require(normalizedTitle.isNotEmpty()) { "Название рецепта не может быть пустым" }

        return Meal(
            idMeal = idProvider(),
            strMeal = normalizedTitle,
            strCategory = draft.category.trim().ifBlank { "Авторский рецепт" },
            strArea = draft.area.trim().ifBlank { "Моя кухня" },
            strInstructions = draft.instructions.trim().ifBlank { "Инструкция пока не добавлена." },
            strMealThumb = draft.imageUrl.trim(),
            ingredients = parseIngredients(draft.ingredientsText)
        )
    }

    fun updateMeal(existing: Meal, draft: CustomMealDraft): Meal {
        val normalizedTitle = draft.title.trim()
        require(normalizedTitle.isNotEmpty()) { "Название рецепта не может быть пустым" }

        return existing.copy(
            strMeal = normalizedTitle,
            strCategory = draft.category.trim().ifBlank { "Авторский рецепт" },
            strArea = draft.area.trim().ifBlank { "Моя кухня" },
            strInstructions = draft.instructions.trim().ifBlank { "Инструкция пока не добавлена." },
            strMealThumb = draft.imageUrl.trim(),
            ingredients = parseIngredients(draft.ingredientsText)
        )
    }

    internal fun parseIngredients(text: String): List<Ingredient> {
        return text
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { line ->
                val separator = when {
                    " - " in line -> " - "
                    ":" in line -> ":"
                    "-" in line -> "-"
                    else -> null
                }

                if (separator == null) {
                    Ingredient(name = line, measure = "")
                } else {
                    val parts = line.split(separator, limit = 2)
                    Ingredient(
                        name = parts.first().trim(),
                        measure = parts.getOrNull(1)?.trim().orEmpty()
                    )
                }
            }
            .filter { it.name.isNotEmpty() }
            .toList()
    }
}
