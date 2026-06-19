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
    var collectionDraft by remember { mutableStateOf(CollectionDraft()) }

    var showCreateMealDialog by remember { mutableStateOf(false) }
    var mealDraft by remember { mutableStateOf(MealDraft()) }
    var mealErrors by remember { mutableStateOf(MealFormErrors()) }
    val ingredientDrafts = remember { mutableStateListOf(IngredientDraft()) }
    var selectedTab by remember { mutableIntStateOf(BOOK_TAB_MY_RECIPES) }
    val clearMealErrors = {
        mealErrors = MealFormErrors()
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
                    onClick = {
                        collectionDraft = CollectionDraft()
                        showCreateCollectionDialog = true
                    },
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
            onDismissRequest = {
                collectionDraft = CollectionDraft()
                showCreateCollectionDialog = false
            },
            title = { Text("Новая коллекция") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = collectionDraft.title,
                        onValueChange = { collectionDraft = collectionDraft.copy(title = it) },
                        label = { Text("Название") }
                    )
                    OutlinedTextField(
                        value = collectionDraft.description,
                        onValueChange = { collectionDraft = collectionDraft.copy(description = it) },
                        label = { Text("Описание") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (collectionDraft.title.isNotBlank()) {
                            onCreateCollection(collectionDraft.title, collectionDraft.description)
                            showCreateCollectionDialog = false
                            collectionDraft = CollectionDraft()
                        }
                    }
                ) {
                    Text("Создать")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    collectionDraft = CollectionDraft()
                    showCreateCollectionDialog = false
                }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showCreateMealDialog) {
        AlertDialog(
            onDismissRequest = {
                clearMealErrors()
                mealDraft = MealDraft()
                showCreateMealDialog = false
            },
            title = { Text("Свой рецепт") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        OutlinedTextField(
                            value = mealDraft.title,
                            onValueChange = {
                                mealDraft = mealDraft.copy(title = it)
                                mealErrors = mealErrors.copy(title = null)
                            },
                            label = { Text("Название рецепта") },
                            isError = mealErrors.title != null,
                            supportingText = mealErrors.title?.let { error ->
                                { Text(error) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = mealDraft.category,
                            onValueChange = {
                                mealDraft = mealDraft.copy(category = it)
                                mealErrors = mealErrors.copy(category = null)
                            },
                            label = { Text("Категория") },
                            isError = mealErrors.category != null,
                            supportingText = mealErrors.category?.let { error ->
                                { Text(error) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = mealDraft.area,
                            onValueChange = {
                                mealDraft = mealDraft.copy(area = it)
                                mealErrors = mealErrors.copy(area = null)
                            },
                            label = { Text("Кухня или страна") },
                            isError = mealErrors.area != null,
                            supportingText = mealErrors.area?.let { error ->
                                { Text(error) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = mealDraft.imageUrl,
                            onValueChange = { mealDraft = mealDraft.copy(imageUrl = it) },
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
                            onChanged = { mealErrors = mealErrors.copy(ingredients = null) }
                        )
                        mealErrors.ingredients?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = mealDraft.instructions,
                            onValueChange = {
                                mealDraft = mealDraft.copy(instructions = it)
                                mealErrors = mealErrors.copy(instructions = null)
                            },
                            label = { Text("Инструкция") },
                            isError = mealErrors.instructions != null,
                            supportingText = mealErrors.instructions?.let { error ->
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
                        mealErrors = MealFormErrors(
                            title = requiredFieldError(mealDraft.title, "название рецепта"),
                            category = requiredFieldError(mealDraft.category, "категорию"),
                            area = requiredFieldError(mealDraft.area, "кухню или страну"),
                            instructions = requiredFieldError(mealDraft.instructions, "инструкцию"),
                            ingredients = if (ingredientDrafts.none { it.name.isNotBlank() }) {
                                "Добавьте хотя бы один ингредиент"
                            } else {
                                null
                            }
                        )

                        if (mealErrors.isValid()) {
                            onCreateCustomMeal(
                                mealDraft.title,
                                mealDraft.category,
                                mealDraft.area,
                                mealDraft.instructions,
                                ingredientDraftsToText(ingredientDrafts),
                                mealDraft.imageUrl
                            )
                            showCreateMealDialog = false
                            mealDraft = MealDraft()
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
                        mealDraft = MealDraft()
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

private data class CollectionDraft(
    val title: String = "",
    val description: String = ""
)

private data class MealDraft(
    val title: String = "",
    val category: String = "",
    val area: String = "",
    val instructions: String = "",
    val imageUrl: String = ""
)

private data class MealFormErrors(
    val title: String? = null,
    val category: String? = null,
    val area: String? = null,
    val instructions: String? = null,
    val ingredients: String? = null
) {
    fun isValid(): Boolean = listOf(title, category, area, instructions, ingredients).all { it == null }
}
