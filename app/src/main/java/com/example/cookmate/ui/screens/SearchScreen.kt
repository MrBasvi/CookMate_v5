package com.example.cookmate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cookmate.ui.state.CookMateUiState
import com.example.cookmate.ui.state.MealUiState

@Composable
fun SearchScreen(
    uiState: CookMateUiState,
    onSearchQueryChange: (String) -> Unit,
    onRetrySearch: () -> Unit,
    onShowOnlyFavoritesChange: (Boolean) -> Unit,
    onMealSelected: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "CookMate",
            fontSize = 30.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = if (uiState.searchQuery.isBlank()) {
                "Введите запрос, чтобы искать рецепты по локальной базе и сети."
            } else {
                "Поиск обновляет локальную базу рецептов."
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Поиск рецептов") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Только избранное")
                Text(
                    text = if (uiState.offlineOnlyMode) {
                        "Офлайн-режим включён в опциях."
                    } else {
                        "Фильтр по сохранённым рецептам."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(
                checked = uiState.showOnlyFavorites,
                onCheckedChange = onShowOnlyFavoritesChange
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (val state = uiState.mealListState) {
            MealUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is MealUiState.Success -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.meals, key = { it.idMeal }) { meal ->
                        val note = uiState.mealNotes[meal.idMeal]
                        val supportingText = buildString {
                            if (meal.idMeal.startsWith("local-")) {
                                append("Мой рецепт")
                            }
                            if (note != null) {
                                if (isNotEmpty()) append(" • ")
                                append("Оценка: ${note.rating}/5")
                            }
                        }.ifBlank { null }

                        MealCardItem(
                            meal = meal,
                            isFavorite = meal.idMeal in uiState.favorites,
                            onMealClick = { onMealSelected(meal.idMeal) },
                            onFavoriteClick = { onToggleFavorite(meal.idMeal) },
                            supportingText = supportingText
                        )
                    }
                }
            }

            is MealUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Не удалось выполнить поиск",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = state.message,
                                textAlign = TextAlign.Center
                            )
                            Button(onClick = onRetrySearch) {
                                Text("Повторить")
                            }
                        }
                    }
                }
            }

            MealUiState.Empty -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (uiState.searchQuery.isBlank()) {
                            "Поиск пока пуст. Введите название блюда, ингредиент или часть рецепта."
                        } else {
                            "По этому запросу ничего не найдено."
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
