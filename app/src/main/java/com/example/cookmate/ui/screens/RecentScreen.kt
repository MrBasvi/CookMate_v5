package com.example.cookmate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cookmate.ui.state.CookMateUiState

@Composable
fun RecentScreen(
    uiState: CookMateUiState,
    onMealSelected: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "История",
            fontSize = 30.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Быстро возвращайтесь к недавно открытым рецептам.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (uiState.recentMeals.isEmpty()) {
            Text(
                text = "Откройте несколько рецептов, чтобы заполнить историю.",
                modifier = Modifier.padding(top = 24.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.recentMeals, key = { it.meal.idMeal }) { recent ->
                    val note = uiState.mealNotes[recent.meal.idMeal]
                    val supportingText = buildString {
                        if (recent.meal.idMeal.startsWith("local-")) {
                            append("Мой рецепт • ")
                        }
                        append("Недавно открывали")
                        if (note != null) {
                            append(" • Оценка: ${note.rating}/5")
                            if (note.noteText.isNotBlank()) {
                                append(" • ${note.noteText.take(32)}")
                            }
                        }
                    }

                    MealCardItem(
                        meal = recent.meal,
                        isFavorite = recent.meal.idMeal in uiState.favorites,
                        onMealClick = { onMealSelected(recent.meal.idMeal) },
                        onFavoriteClick = { onToggleFavorite(recent.meal.idMeal) },
                        supportingText = supportingText
                    )
                }
            }
        }
    }
}
