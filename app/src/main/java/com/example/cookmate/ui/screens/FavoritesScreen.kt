package com.example.cookmate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cookmate.ui.state.CookMateUiState

private const val BOOK_TAB_MY_RECIPES = 0
private const val BOOK_TAB_FAVORITES = 1
private const val BOOK_TAB_COLLECTIONS = 2

@Composable
fun FavoritesScreen(
    uiState: CookMateUiState,
    onMealSelected: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onCreateCustomMeal: (String, String, String, String, String, String) -> Unit,
    onCollectionSelected: (Long) -> Unit,
    onCreateCollection: (String, String) -> Unit,
    onDeleteCollection: (Long) -> Unit,
    onToggleCollectionPin: (Long) -> Unit
) {
    var showCreateCollectionDialog by remember { mutableStateOf(false) }
    var collectionTitleDraft by remember { mutableStateOf("") }
    var collectionDescriptionDraft by remember { mutableStateOf("") }

    var showCreateMealDialog by remember { mutableStateOf(false) }
    var mealTitleDraft by remember { mutableStateOf("") }
    var mealCategoryDraft by remember { mutableStateOf("") }
    var mealAreaDraft by remember { mutableStateOf("") }
    var mealInstructionsDraft by remember { mutableStateOf("") }
    var mealImageUrlDraft by remember { mutableStateOf("") }
    var mealTitleError by remember { mutableStateOf<String?>(null) }
    var mealCategoryError by remember { mutableStateOf<String?>(null) }
    var mealAreaError by remember { mutableStateOf<String?>(null) }
    var mealInstructionsError by remember { mutableStateOf<String?>(null) }
    var mealIngredientsError by remember { mutableStateOf<String?>(null) }
    val ingredientDrafts = remember { mutableStateListOf(IngredientDraft()) }
    var selectedTab by remember { mutableIntStateOf(BOOK_TAB_MY_RECIPES) }
    val clearMealErrors = {
        mealTitleError = null
        mealCategoryError = null
        mealAreaError = null
        mealInstructionsError = null
        mealIngredientsError = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Моя кулинарная книга",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Коллекции, избранное и личные рецепты, которые доступны офлайн.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedTab == BOOK_TAB_MY_RECIPES,
                    onClick = { selectedTab = BOOK_TAB_MY_RECIPES },
                    label = {
                        Text(
                            text = "Мои рецепты",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
            item {
                FilterChip(
                    selected = selectedTab == BOOK_TAB_FAVORITES,
                    onClick = { selectedTab = BOOK_TAB_FAVORITES },
                    label = {
                        Text(
                            text = "Избранное",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
            item {
                FilterChip(
                    selected = selectedTab == BOOK_TAB_COLLECTIONS,
                    onClick = { selectedTab = BOOK_TAB_COLLECTIONS },
                    label = {
                        Text(
                            text = "Коллекции",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            BOOK_TAB_MY_RECIPES -> {
                Button(
                    onClick = {
                        clearMealErrors()
                        showCreateMealDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Добавить свой рецепт")
                }
            }

            BOOK_TAB_COLLECTIONS -> {
                Button(
                    onClick = { showCreateCollectionDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Создать коллекцию")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (selectedTab) {
                BOOK_TAB_MY_RECIPES -> {
                    item {
                        Text("Мои рецепты", style = MaterialTheme.typography.titleMedium)
                    }

                    if (uiState.localMeals.isEmpty()) {
                        item {
                            Text(
                                text = "Здесь будут ваши собственные рецепты. Они сохраняются локально и не добавляются в избранное автоматически.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(uiState.localMeals, key = { it.idMeal }) { meal ->
                            val supportingText = uiState.mealNotes[meal.idMeal]?.let { note ->
                                "Мой рецепт • Оценка: ${note.rating}/5"
                            } ?: "Мой рецепт"

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

                BOOK_TAB_FAVORITES -> {
                    item {
                        Text("Избранное", style = MaterialTheme.typography.titleMedium)
                    }

                    if (uiState.favoriteMeals.isEmpty()) {
                        item {
                            Text(
                                text = "Пока нет избранных рецептов. Добавляйте сюда только то, что хотите держать под рукой отдельно.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(uiState.favoriteMeals, key = { it.idMeal }) { meal ->
                            val supportingText = buildString {
                                if (meal.idMeal.startsWith("local-")) {
                                    append("Мой рецепт")
                                }
                                uiState.mealNotes[meal.idMeal]?.let { note ->
                                    if (isNotEmpty()) append(" • ")
                                    append("Оценка: ${note.rating}/5")
                                }
                            }.ifBlank { null }

                            MealCardItem(
                                meal = meal,
                                isFavorite = true,
                                onMealClick = { onMealSelected(meal.idMeal) },
                                onFavoriteClick = { onToggleFavorite(meal.idMeal) },
                                supportingText = supportingText
                            )
                        }
                    }
                }

                BOOK_TAB_COLLECTIONS -> {
                    item {
                        Text("Коллекции", style = MaterialTheme.typography.titleMedium)
                    }

                    if (uiState.collections.isEmpty()) {
                        item {
                            Text(
                                text = "Создайте первую коллекцию, чтобы собрать свои тематические наборы рецептов.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(uiState.collections, key = { it.collectionId }) { collection ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.fillMaxWidth(0.72f)) {
                                            Text(
                                                text = collection.title,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            if (collection.description.isNotBlank()) {
                                                Text(
                                                    text = collection.description,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = "${collection.mealCount} рецептов",
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        IconButton(onClick = { onToggleCollectionPin(collection.collectionId) }) {
                                            Icon(
                                                imageVector = Icons.Default.PushPin,
                                                contentDescription = "Закрепить коллекцию",
                                                tint = if (collection.isPinned) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }
                                        IconButton(onClick = { onDeleteCollection(collection.collectionId) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Удалить коллекцию"
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(onClick = { onCollectionSelected(collection.collectionId) }) {
                                        Text("Открыть коллекцию")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateCollectionDialog) {
        AlertDialog(
            onDismissRequest = { showCreateCollectionDialog = false },
            title = { Text("Новая коллекция") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = collectionTitleDraft,
                        onValueChange = { collectionTitleDraft = it },
                        label = { Text("Название") }
                    )
                    OutlinedTextField(
                        value = collectionDescriptionDraft,
                        onValueChange = { collectionDescriptionDraft = it },
                        label = { Text("Описание") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (collectionTitleDraft.isNotBlank()) {
                            onCreateCollection(collectionTitleDraft, collectionDescriptionDraft)
                            showCreateCollectionDialog = false
                            collectionTitleDraft = ""
                            collectionDescriptionDraft = ""
                        }
                    }
                ) {
                    Text("Создать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateCollectionDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showCreateMealDialog) {
        AlertDialog(
            onDismissRequest = {
                clearMealErrors()
                showCreateMealDialog = false
            },
            title = { Text("Свой рецепт") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        OutlinedTextField(
                            value = mealTitleDraft,
                            onValueChange = {
                                mealTitleDraft = it
                                mealTitleError = null
                            },
                            label = { Text("Название рецепта") },
                            isError = mealTitleError != null,
                            supportingText = mealTitleError?.let { error ->
                                { Text(error) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = mealCategoryDraft,
                            onValueChange = {
                                mealCategoryDraft = it
                                mealCategoryError = null
                            },
                            label = { Text("Категория") },
                            isError = mealCategoryError != null,
                            supportingText = mealCategoryError?.let { error ->
                                { Text(error) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = mealAreaDraft,
                            onValueChange = {
                                mealAreaDraft = it
                                mealAreaError = null
                            },
                            label = { Text("Кухня или страна") },
                            isError = mealAreaError != null,
                            supportingText = mealAreaError?.let { error ->
                                { Text(error) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = mealImageUrlDraft,
                            onValueChange = { mealImageUrlDraft = it },
                            label = { Text("URL обложки") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Text("Ингредиенты", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Каждый ингредиент добавляется отдельной позицией.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    item {
                        IngredientEditor(
                            items = ingredientDrafts,
                            modifier = Modifier.fillMaxWidth(),
                            onChanged = { mealIngredientsError = null }
                        )
                        mealIngredientsError?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = mealInstructionsDraft,
                            onValueChange = {
                                mealInstructionsDraft = it
                                mealInstructionsError = null
                            },
                            label = { Text("Инструкция") },
                            isError = mealInstructionsError != null,
                            supportingText = mealInstructionsError?.let { error ->
                                { Text(error) }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mealTitleError = requiredFieldError(
                            value = mealTitleDraft,
                            fieldName = "название рецепта"
                        )
                        mealCategoryError = requiredFieldError(
                            value = mealCategoryDraft,
                            fieldName = "категорию"
                        )
                        mealAreaError = requiredFieldError(
                            value = mealAreaDraft,
                            fieldName = "кухню или страну"
                        )
                        mealInstructionsError = requiredFieldError(
                            value = mealInstructionsDraft,
                            fieldName = "инструкцию"
                        )
                        mealIngredientsError = if (ingredientDrafts.none { it.name.isNotBlank() }) {
                            "Добавьте хотя бы один ингредиент"
                        } else {
                            null
                        }

                        val hasErrors = listOf(
                            mealTitleError,
                            mealCategoryError,
                            mealAreaError,
                            mealInstructionsError,
                            mealIngredientsError
                        ).any { it != null }

                        if (!hasErrors) {
                            onCreateCustomMeal(
                                mealTitleDraft,
                                mealCategoryDraft,
                                mealAreaDraft,
                                mealInstructionsDraft,
                                ingredientDraftsToText(ingredientDrafts),
                                mealImageUrlDraft
                            )
                            showCreateMealDialog = false
                            mealTitleDraft = ""
                            mealCategoryDraft = ""
                            mealAreaDraft = ""
                            mealInstructionsDraft = ""
                            mealImageUrlDraft = ""
                            clearMealErrors()
                            ingredientDrafts.clear()
                            ingredientDrafts.add(IngredientDraft())
                        }
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        clearMealErrors()
                        showCreateMealDialog = false
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

private fun requiredFieldError(value: String, fieldName: String): String? {
    return if (value.isBlank()) {
        "Заполните $fieldName"
    } else {
        null
    }
}
