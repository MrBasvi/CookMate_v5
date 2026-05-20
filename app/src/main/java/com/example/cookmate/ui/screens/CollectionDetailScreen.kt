package com.example.cookmate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cookmate.ui.state.CookMateUiState

@Composable
fun CollectionDetailScreen(
    uiState: CookMateUiState,
    onBackClick: () -> Unit,
    onMealSelected: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRemoveMeal: (Long, String) -> Unit,
    onUpdateCollection: (Long, String, String) -> Unit
) {
    val collection = uiState.selectedCollection ?: return
    var showEditDialog by remember(collection.collectionId) { mutableStateOf(false) }
    var titleDraft by remember(collection.collectionId, collection.updatedAt) { mutableStateOf(collection.title) }
    var descriptionDraft by remember(collection.collectionId, collection.updatedAt) { mutableStateOf(collection.description) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Назад"
                )
                Text("Назад")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = collection.title,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = { showEditDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Редактировать коллекцию"
                )
            }
        }

        if (collection.description.isNotBlank()) {
            Text(
                text = collection.description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (collection.meals.isEmpty()) {
            Text("Пока в этой коллекции нет рецептов.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(collection.meals) { meal ->
                    Column {
                        MealCardItem(
                            meal = meal,
                            isFavorite = meal.idMeal in uiState.favorites,
                            onMealClick = { onMealSelected(meal.idMeal) },
                            onFavoriteClick = { onToggleFavorite(meal.idMeal) },
                            supportingText = uiState.mealNotes[meal.idMeal]?.let { "Оценка: ${it.rating}/5" }
                        )
                        TextButton(onClick = { onRemoveMeal(collection.collectionId, meal.idMeal) }) {
                            Text("Убрать из коллекции")
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Редактировать коллекцию") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = titleDraft,
                        onValueChange = { titleDraft = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Название") }
                    )
                    OutlinedTextField(
                        value = descriptionDraft,
                        onValueChange = { descriptionDraft = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Описание") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateCollection(collection.collectionId, titleDraft, descriptionDraft)
                        showEditDialog = false
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}
