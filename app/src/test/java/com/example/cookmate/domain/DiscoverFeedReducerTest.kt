package com.example.cookmate.domain

import com.example.cookmate.data.model.Meal
import com.example.cookmate.ui.state.MealUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiscoverFeedReducerTest {

    private val reducer = DiscoverFeedReducer()
    private val meal = Meal(
        idMeal = "1",
        strMeal = "Chicken Soup",
        strCategory = "Soup",
        strArea = "Global",
        strInstructions = "Cook it",
        strMealThumb = "thumb"
    )

    @Test
    fun blankQuery_returnsRecentMeals() {
        val state = reducer.reduce(
            query = "",
            cachedMatches = emptyList(),
            recentMeals = listOf(meal),
            favoriteIds = emptySet(),
            showOnlyFavorites = false,
            remoteState = RemoteSearchState.Idle
        )

        assertTrue(state is MealUiState.Success)
        assertEquals(listOf("1"), (state as MealUiState.Success).meals.map { it.idMeal })
    }

    @Test
    fun favoriteFilter_keepsOnlyFavoriteMeals() {
        val state = reducer.reduce(
            query = "chicken",
            cachedMatches = listOf(meal, meal.copy(idMeal = "2", strMeal = "Fish Stew")),
            recentMeals = emptyList(),
            favoriteIds = setOf("1"),
            showOnlyFavorites = true,
            remoteState = RemoteSearchState.Idle
        )

        assertTrue(state is MealUiState.Success)
        assertEquals(listOf("1"), (state as MealUiState.Success).meals.map { it.idMeal })
    }

    @Test
    fun remoteError_withoutCache_surfacesError() {
        val state = reducer.reduce(
            query = "missing",
            cachedMatches = emptyList(),
            recentMeals = emptyList(),
            favoriteIds = emptySet(),
            showOnlyFavorites = false,
            remoteState = RemoteSearchState.Error("Network down")
        )

        assertTrue(state is MealUiState.Error)
        assertEquals("Network down", (state as MealUiState.Error).message)
    }
}
