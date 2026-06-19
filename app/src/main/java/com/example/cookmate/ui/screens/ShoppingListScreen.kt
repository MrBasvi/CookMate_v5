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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cookmate.ui.state.CookMateUiState

private const val SHOPPING_TAB_LIST = 0
private const val SHOPPING_TAB_ADD = 1

@Composable
fun ShoppingListScreen(
    uiState: CookMateUiState,
    onAddManualItem: (String, String) -> Unit,
    onSetChecked: (Long, Boolean) -> Unit,
    onRemoveItem: (Long) -> Unit,
    onClearChecked: () -> Unit,
    onClearAll: () -> Unit
) {
    var ingredientDraft by remember { mutableStateOf("") }
    var measureDraft by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(SHOPPING_TAB_LIST) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Покупки",
            fontSize = 30.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Отдельный раздел для списка покупок и ручного добавления позиций.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedTab == SHOPPING_TAB_LIST,
                onClick = { selectedTab = SHOPPING_TAB_LIST },
                label = { Text("Список") }
            )
            FilterChip(
                selected = selectedTab == SHOPPING_TAB_ADD,
                onClick = { selectedTab = SHOPPING_TAB_ADD },
                label = { Text("Добавить") }
            )
        }

        if (selectedTab == SHOPPING_TAB_ADD) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Добавить вручную", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = ingredientDraft,
                        onValueChange = { ingredientDraft = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Ингредиент") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = measureDraft,
                        onValueChange = { measureDraft = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Количество или мера") },
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            onAddManualItem(ingredientDraft, measureDraft)
                            if (ingredientDraft.isNotBlank()) {
                                ingredientDraft = ""
                                measureDraft = ""
                                selectedTab = SHOPPING_TAB_LIST
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Добавить в список")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onClearChecked,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Очистить купленные")
                }
                OutlinedButton(
                    onClick = onClearAll,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Очистить всё")
                }
            }

            if (uiState.shoppingListItems.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Список покупок пуст.")
                    Text(
                        text = "Добавьте ингредиенты из рецепта или создайте свою позицию на соседней вкладке.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.shoppingListItems, key = { it.itemId }) { item ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Checkbox(
                                    checked = item.isChecked,
                                    onCheckedChange = { checked -> onSetChecked(item.itemId, checked) }
                                )

                                Column(modifier = Modifier.fillMaxWidth(0.72f)) {
                                    Text(
                                        text = item.ingredientName,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    val subtitle = buildString {
                                        if (item.measure.isNotBlank()) {
                                            append(item.measure)
                                        }
                                        if (item.quantityCount > 1) {
                                            if (isNotEmpty()) append(" • ")
                                            append("x${item.quantityCount}")
                                        }
                                    }
                                    if (subtitle.isNotBlank()) {
                                        Text(
                                            text = subtitle,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(onClick = { onRemoveItem(item.itemId) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Удалить позицию"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
