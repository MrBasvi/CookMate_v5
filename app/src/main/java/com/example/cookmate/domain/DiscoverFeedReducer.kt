package com.example.cookmate.domain

import com.example.cookmate.data.model.Meal
import com.example.cookmate.ui.state.MealUiState
import javax.inject.Inject

class DiscoverFeedReducer @Inject constructor() {

    fun reduce(
        query: String,
        cachedMatches: List<Meal>,
        recentMeals: List<Meal>,
        favoriteIds: Set<String>,
        showOnlyFavorites: Boolean,
        remoteState: RemoteSearchState
    ): MealUiState {
        if (query.isBlank()) {
            return MealUiState.Empty
        }

        val baseMeals = if (query.isBlank()) {
            recentMeals.filterNot { it.idMeal.startsWith("local-") }
        } else {
            cachedMatches
        }
        val visibleMeals = if (showOnlyFavorites) {
            baseMeals.filter { it.idMeal in favoriteIds }
        } else {
            baseMeals
        }

        if (visibleMeals.isNotEmpty()) {
            return MealUiState.Success(visibleMeals)
        }

        return when {
            query.isBlank() -> MealUiState.Empty
            remoteState is RemoteSearchState.Loading -> MealUiState.Loading
            remoteState is RemoteSearchState.Error -> MealUiState.Error(remoteState.message)
            remoteState is RemoteSearchState.Empty -> MealUiState.Empty
            showOnlyFavorites -> MealUiState.Empty
            else -> MealUiState.Empty
        }
    }
}

sealed interface RemoteSearchState {
    data object Idle : RemoteSearchState
    data object Loading : RemoteSearchState
    data object Empty : RemoteSearchState
    data class Error(val message: String) : RemoteSearchState
}
