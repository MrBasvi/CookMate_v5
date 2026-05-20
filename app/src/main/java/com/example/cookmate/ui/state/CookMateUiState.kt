package com.example.cookmate.ui.state

import com.example.cookmate.data.model.Meal
import com.example.cookmate.data.model.MealCollectionDetail
import com.example.cookmate.data.model.MealCollectionMembership
import com.example.cookmate.data.model.MealCollectionSummary
import com.example.cookmate.data.model.MealNote
import com.example.cookmate.data.model.RecentMeal
import com.example.cookmate.data.model.ShoppingListItem

sealed class MealUiState {
    data object Loading : MealUiState()
    data class Success(val meals: List<Meal>) : MealUiState()
    data class Error(val message: String) : MealUiState()
    data object Empty : MealUiState()
}

sealed class MealDetailUiState {
    data object Loading : MealDetailUiState()
    data class Success(val meal: Meal) : MealDetailUiState()
    data class Error(val message: String) : MealDetailUiState()
}

data class CookMateUiState(
    val searchQuery: String = "",
    val mealListState: MealUiState = MealUiState.Empty,
    val mealDetailState: MealDetailUiState? = null,
    val selectedMealId: String? = null,
    val selectedCollectionId: Long? = null,
    val favorites: List<String> = emptyList(),
    val favoriteMeals: List<Meal> = emptyList(),
    val localMeals: List<Meal> = emptyList(),
    val allMeals: List<Meal> = emptyList(),
    val showOnlyFavorites: Boolean = false,
    val collections: List<MealCollectionSummary> = emptyList(),
    val selectedCollection: MealCollectionDetail? = null,
    val selectedMealMemberships: List<MealCollectionMembership> = emptyList(),
    val recentMeals: List<RecentMeal> = emptyList(),
    val shoppingListItems: List<ShoppingListItem> = emptyList(),
    val mealNotes: Map<String, MealNote> = emptyMap(),
    val selectedMealNote: MealNote? = null,
    val offlineOnlyMode: Boolean = false,
    val backgroundSyncEnabled: Boolean = true,
    val historyLimit: Int = 30,
    val startDestination: String = "",
    val syncStatusMessage: String? = null
)
