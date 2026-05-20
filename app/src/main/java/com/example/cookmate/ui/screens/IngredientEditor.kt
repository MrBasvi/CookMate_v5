package com.example.cookmate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class IngredientDraft(
    val name: String = "",
    val measure: String = ""
)

@Composable
fun IngredientEditor(
    items: SnapshotStateList<IngredientDraft>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEachIndexed { index, item ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = item.name,
                    onValueChange = { items[index] = item.copy(name = it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ингредиент") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = item.measure,
                    onValueChange = { items[index] = item.copy(measure = it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Количество") },
                    singleLine = true
                )
                IconButton(
                    onClick = {
                        if (items.size > 1) {
                            items.removeAt(index)
                        } else {
                            items[index] = IngredientDraft()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить ингредиент"
                    )
                }
            }
        }

        TextButton(onClick = { items.add(IngredientDraft()) }) {
            Text("Добавить ингредиент")
        }
    }
}

fun ingredientDraftsToText(items: List<IngredientDraft>): String =
    items
        .mapNotNull { item ->
            val name = item.name.trim()
            val measure = item.measure.trim()
            when {
                name.isEmpty() -> null
                measure.isEmpty() -> name
                else -> "$name - $measure"
            }
        }
        .joinToString("\n")

fun ingredientDraftsFromText(text: String): List<IngredientDraft> {
    val rows = text
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { line ->
            val parts = line.split(" - ", limit = 2)
            if (parts.size == 2) {
                IngredientDraft(parts[0].trim(), parts[1].trim())
            } else {
                IngredientDraft(name = line)
            }
        }
        .toList()

    return if (rows.isEmpty()) listOf(IngredientDraft()) else rows
}
