package com.example.cookmate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cookmate.navigation.CookMateRoutes
import com.example.cookmate.ui.state.CookMateUiState
import com.example.cookmate.ui.state.SyncUiState

@Composable
fun SettingsScreen(
    uiState: CookMateUiState,
    onOfflineOnlyChange: (Boolean) -> Unit,
    onBackgroundSyncChange: (Boolean) -> Unit,
    onHistoryLimitChange: (Int) -> Unit,
    onStartDestinationChange: (String) -> Unit,
    onSyncNow: () -> Unit,
    onClearMessage: () -> Unit
) {
    val selectedStartDestination = if (uiState.startDestination.isBlank()) {
        CookMateRoutes.SEARCH
    } else {
        uiState.startDestination
    }
    val syncMessage = uiState.syncStatusMessage
    val isSyncing = uiState.syncUiState is SyncUiState.Running

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = "Опции",
                    fontSize = 30.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Управление офлайн-режимом, историей и обновлением сохранённых рецептов.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Режим хранения", style = MaterialTheme.typography.titleMedium)

                    SettingSwitchRow(
                        title = "Только офлайн",
                        subtitle = "Поиск использует только сохранённые рецепты и историю, без новых запросов в сеть.",
                        checked = uiState.offlineOnlyMode,
                        onCheckedChange = onOfflineOnlyChange
                    )

                    SettingSwitchRow(
                        title = "Фоновая синхронизация",
                        subtitle = "Раз в несколько часов обновляет сохранённые рецепты из избранного, коллекций и истории.",
                        checked = uiState.backgroundSyncEnabled,
                        onCheckedChange = onBackgroundSyncChange
                    )
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("История", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Сколько последних рецептов хранить в разделе «История».",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15, 30, 50).forEach { limit ->
                            FilterChip(
                                selected = uiState.historyLimit == limit,
                                onClick = { onHistoryLimitChange(limit) },
                                label = { Text(limit.toString()) }
                            )
                        }
                    }
                    Text(
                        text = "Сейчас: ${uiState.historyLimit}",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Стартовый экран", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Какой раздел открывать первым только после следующего запуска приложения.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    listOf(
                        CookMateRoutes.SEARCH to "Поиск",
                        CookMateRoutes.FAVORITES to "Книга",
                        CookMateRoutes.RECENT to "История",
                        CookMateRoutes.SHOPPING to "Покупки"
                    ).forEach { option ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedStartDestination == option.first,
                                onClick = { onStartDestinationChange(option.first) }
                            )
                            Text(option.second)
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Синхронизация сейчас", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Принудительно обновляет сохранённые рецепты сразу, не дожидаясь фонового запуска.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onSyncNow,
                        enabled = !isSyncing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Синхронизировать сейчас")
                    }

                    syncMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Button(onClick = onClearMessage) {
                            Text("Очистить статус")
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Фоновые обновления используют WorkManager и не мешают обычной работе приложения.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .padding(end = 12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
