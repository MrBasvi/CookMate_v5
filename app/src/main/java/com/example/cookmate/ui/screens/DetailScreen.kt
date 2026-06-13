package com.example.cookmate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.cookmate.ui.state.CookMateUiState
import com.example.cookmate.ui.state.MealDetailUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    uiState: CookMateUiState,
    onBackClick: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRetry: (String) -> Unit,
    onClearDetail: () -> Unit,
    onSaveNote: (mealId: String, note: String, rating: Int) -> Unit,
    onAddIngredientsToShoppingList: (String) -> Unit,
    onToggleMealInCollection: (collectionId: Long, mealId: String) -> Unit,
    onUpdateCustomMeal: (String, String, String, String, String, String, String) -> Unit,
    onDeleteCustomMeal: (String) -> Unit
) {
    val mealId = uiState.selectedMealId ?: return
    var noteDraft by remember(mealId, uiState.selectedMealNote?.updatedAt) {
        mutableStateOf(uiState.selectedMealNote?.noteText.orEmpty())
    }
    var ratingDraft by remember(mealId, uiState.selectedMealNote?.updatedAt) {
        mutableIntStateOf(uiState.selectedMealNote?.rating ?: 0)
    }

    val currentMeal = (uiState.mealDetailState as? MealDetailUiState.Success)?.meal
    val isLocalMeal = mealId.startsWith("local-")
    val shoppingFeedback = uiState.shoppingFeedbackMessage

    var showEditDialog by remember(mealId) { mutableStateOf(false) }
    var showDeleteDialog by remember(mealId) { mutableStateOf(false) }
    var titleDraft by remember(mealId) { mutableStateOf("") }
    var categoryDraft by remember(mealId) { mutableStateOf("") }
    var areaDraft by remember(mealId) { mutableStateOf("") }
    var instructionsDraft by remember(mealId) { mutableStateOf("") }
    var imageUrlDraft by remember(mealId) { mutableStateOf("") }
    val ingredientDrafts = remember(mealId) { mutableStateListOf(IngredientDraft()) }

    LaunchedEffect(currentMeal?.idMeal, showEditDialog) {
        if (showEditDialog && currentMeal != null) {
            titleDraft = currentMeal.strMeal
            categoryDraft = currentMeal.strCategory
            areaDraft = currentMeal.strArea
            instructionsDraft = currentMeal.strInstructions
            imageUrlDraft = currentMeal.strMealThumb
            ingredientDrafts.clear()
            ingredientDrafts.addAll(
                ingredientDraftsFromText(
                    currentMeal.ingredients.joinToString("\n") { ingredient ->
                        if (ingredient.measure.isBlank()) ingredient.name else "${ingredient.name} - ${ingredient.measure}"
                    }
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = {
                    onClearDetail()
                    onBackClick()
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Назад"
                )
                Text("Назад")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (isLocalMeal) {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Редактировать рецепт"
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить рецепт",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                IconButton(onClick = { onToggleFavorite(mealId) }) {
                    Icon(
                        imageVector = if (mealId in uiState.favorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Переключить избранное",
                        tint = if (mealId in uiState.favorites) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }

        when (val detailState = uiState.mealDetailState) {
            MealDetailUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is MealDetailUiState.Success -> {
                val meal = detailState.meal

                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        if (meal.strMealThumb.isBlank()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Для этого рецепта нет обложки")
                                }
                            }
                        } else {
                            AsyncImage(
                                model = meal.strMealThumb,
                                contentDescription = meal.strMeal,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    item {
                        Text(
                            text = meal.strMeal,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${meal.strCategory} • ${meal.strArea}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isLocalMeal) {
                            Spacer(modifier = Modifier.height(6.dp))
                            AssistChip(
                                onClick = {},
                                label = { Text("Мой рецепт") }
                            )
                        }
                    }

                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Моя оценка и заметка", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    (1..5).forEach { rating ->
                                        IconButton(onClick = { ratingDraft = rating }) {
                                            Icon(
                                                imageVector = if (rating <= ratingDraft) {
                                                    Icons.Default.Star
                                                } else {
                                                    Icons.Default.StarBorder
                                                },
                                                contentDescription = "Оценка $rating из 5",
                                                tint = if (rating <= ratingDraft) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = if (ratingDraft == 0) {
                                        "Оценка ещё не выбрана"
                                    } else {
                                        "Оценка: $ratingDraft из 5"
                                    },
                                    color = if (ratingDraft == 0) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = noteDraft,
                                    onValueChange = { noteDraft = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3,
                                    label = { Text("Что важно запомнить на будущее?") }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { onSaveNote(mealId, noteDraft, ratingDraft) }) {
                                    Text("Сохранить заметку")
                                }
                            }
                        }
                    }

                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Коллекции", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                if (uiState.selectedMealMemberships.isEmpty()) {
                                    Text(
                                        text = "Сначала создайте коллекцию на экране книги.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        uiState.selectedMealMemberships.forEach { membership ->
                                            AssistChip(
                                                onClick = {
                                                    onToggleMealInCollection(membership.collectionId, mealId)
                                                },
                                                label = { Text(membership.title) },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = if (membership.containsMeal) {
                                                        MaterialTheme.colorScheme.secondaryContainer
                                                    } else {
                                                        MaterialTheme.colorScheme.surfaceVariant
                                                    }
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Ингредиенты", style = MaterialTheme.typography.titleMedium)
                                    TextButton(onClick = { onAddIngredientsToShoppingList(mealId) }) {
                                        Text("В покупки")
                                    }
                                }
                                shoppingFeedback?.let { message ->
                                    Text(
                                        text = message,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                meal.ingredients.forEach { ingredient ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(ingredient.name)
                                        Text(
                                            ingredient.measure,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Инструкция", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(meal.strInstructions)
                            }
                        }
                    }
                }
            }

            is MealDetailUiState.Error -> {
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
                            Text("Не удалось загрузить рецепт")
                            Text(detailState.message, textAlign = TextAlign.Center)
                            Button(onClick = { onRetry(mealId) }) {
                                Text("Повторить")
                            }
                        }
                    }
                }
            }

            null -> Unit
        }
    }

    if (showEditDialog && currentMeal != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Редактировать свой рецепт") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        OutlinedTextField(
                            value = titleDraft,
                            onValueChange = { titleDraft = it },
                            label = { Text("Название") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = categoryDraft,
                            onValueChange = { categoryDraft = it },
                            label = { Text("Категория") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = areaDraft,
                            onValueChange = { areaDraft = it },
                            label = { Text("Кухня или страна") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = imageUrlDraft,
                            onValueChange = { imageUrlDraft = it },
                            label = { Text("URL обложки") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Text("Ингредиенты", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Каждый ингредиент редактируется отдельной позицией.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    item {
                        IngredientEditor(items = ingredientDrafts, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(
                            value = instructionsDraft,
                            onValueChange = { instructionsDraft = it },
                            label = { Text("Инструкция") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateCustomMeal(
                            mealId,
                            titleDraft,
                            categoryDraft,
                            areaDraft,
                            instructionsDraft,
                            ingredientDraftsToText(ingredientDrafts),
                            imageUrlDraft
                        )
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

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить свой рецепт?") },
            text = {
                Text("Рецепт исчезнет из локальной базы, недавних, коллекций и избранного.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteCustomMeal(mealId)
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}
