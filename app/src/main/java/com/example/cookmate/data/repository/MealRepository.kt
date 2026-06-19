package com.example.cookmate.data.repository

import com.example.cookmate.data.api.MealApiService
import com.example.cookmate.data.api.RemoteMeal
import com.example.cookmate.data.model.Ingredient
import com.example.cookmate.data.model.Meal
import com.example.cookmate.domain.MealDetailsFetcher
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MealRepository @Inject constructor(
    private val apiService: MealApiService
) : MealDetailsFetcher {
    private companion object {
        const val FALLBACK_VALUE = "Не указано"
    }

    suspend fun searchMealsByName(name: String): List<Meal> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchMealsByName(name)
            response.meals?.map { it.toMeal() } ?: emptyList()
        } catch (throwable: CancellationException) {
            throw throwable
        } catch (throwable: Exception) {
            throw Exception("Search failed: ${throwable.localizedMessage}")
        }
    }

    override suspend fun getMealDetails(mealId: String): Meal = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMealDetails(mealId)
            val remoteMeal = response.meals?.firstOrNull()
                ?: throw Exception("Meal was not found")
            remoteMeal.toMeal()
        } catch (throwable: CancellationException) {
            throw throwable
        } catch (throwable: Exception) {
            throw Exception("Details failed: ${throwable.localizedMessage}")
        }
    }

    private fun RemoteMeal.toMeal(): Meal {
        val ingredients = mutableListOf<Ingredient>()
        val ingredientValues = listOf(
            strIngredient1, strIngredient2, strIngredient3, strIngredient4, strIngredient5,
            strIngredient6, strIngredient7, strIngredient8, strIngredient9, strIngredient10,
            strIngredient11, strIngredient12, strIngredient13, strIngredient14, strIngredient15,
            strIngredient16, strIngredient17, strIngredient18, strIngredient19, strIngredient20
        )
        val measureValues = listOf(
            strMeasure1, strMeasure2, strMeasure3, strMeasure4, strMeasure5,
            strMeasure6, strMeasure7, strMeasure8, strMeasure9, strMeasure10,
            strMeasure11, strMeasure12, strMeasure13, strMeasure14, strMeasure15,
            strMeasure16, strMeasure17, strMeasure18, strMeasure19, strMeasure20
        )

        ingredientValues.indices.forEach { index ->
            val ingredient = ingredientValues[index]
            val measure = measureValues.getOrNull(index)
            if (!ingredient.isNullOrBlank()) {
                ingredients += Ingredient(
                    name = ingredient.trim(),
                    measure = measure?.trim().orEmpty()
                )
            }
        }

        return Meal(
            idMeal = idMeal,
            strMeal = strMeal.orFallback(FALLBACK_VALUE),
            strCategory = strCategory.orFallback(FALLBACK_VALUE),
            strArea = strArea.orFallback(FALLBACK_VALUE),
            strInstructions = strInstructions.orFallback(FALLBACK_VALUE),
            strMealThumb = strMealThumb.orEmpty(),
            ingredients = ingredients
        )
    }

    private fun String?.orFallback(defaultValue: String): String =
        this?.trim()?.takeIf { it.isNotEmpty() } ?: defaultValue
}
