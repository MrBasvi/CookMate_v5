package com.example.cookmate.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cookmate.ui.screens.CollectionDetailScreen
import com.example.cookmate.ui.screens.DetailScreen
import com.example.cookmate.ui.screens.FavoritesScreen
import com.example.cookmate.ui.screens.RecentScreen
import com.example.cookmate.ui.screens.SearchScreen
import com.example.cookmate.ui.screens.SettingsScreen
import com.example.cookmate.ui.screens.ShoppingListScreen
import com.example.cookmate.ui.state.CookMateUiState
import com.example.cookmate.ui.viewmodel.CookMateViewModel

object CookMateRoutes {
    const val SEARCH = "search"
    const val FAVORITES = "favorites"
    const val RECENT = "recent"
    const val SHOPPING = "shopping"
    const val SETTINGS = "settings"
    const val DETAIL = "detail"
    const val COLLECTION = "collection"
}

@Composable
fun CookMateNavHost(
    navController: NavHostController = rememberNavController(),
    viewModel: CookMateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState(initial = CookMateUiState())
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val startDestinationApplied = remember { mutableStateOf(false) }

    val bottomItems = listOf(
        BottomNavItem(CookMateRoutes.SEARCH, "Поиск", Icons.Default.Search),
        BottomNavItem(CookMateRoutes.FAVORITES, "Книга", Icons.Default.Favorite),
        BottomNavItem(CookMateRoutes.RECENT, "История", Icons.Default.History),
        BottomNavItem(CookMateRoutes.SHOPPING, "Покупки", Icons.Default.ShoppingCart),
        BottomNavItem(CookMateRoutes.SETTINGS, "Опции", Icons.Default.Settings)
    )

    LaunchedEffect(uiState.startDestination) {
        val targetRoute = uiState.startDestination
        if (!startDestinationApplied.value && targetRoute.isNotBlank()) {
            startDestinationApplied.value = true
            if (targetRoute != CookMateRoutes.SEARCH) {
                navController.navigate(targetRoute) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                bottomItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = {
                            Text(
                                text = item.label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = CookMateRoutes.SEARCH,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(CookMateRoutes.SEARCH) {
                SearchScreen(
                    uiState = uiState,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onRetrySearch = viewModel::retrySearch,
                    onShowOnlyFavoritesChange = viewModel::setShowOnlyFavorites,
                    onMealSelected = { mealId ->
                        navController.navigate("${CookMateRoutes.DETAIL}/$mealId")
                    },
                    onToggleFavorite = viewModel::toggleFavorite
                )
            }

            composable(CookMateRoutes.FAVORITES) {
                FavoritesScreen(
                    uiState = uiState,
                    onMealSelected = { mealId ->
                        navController.navigate("${CookMateRoutes.DETAIL}/$mealId")
                    },
                    onToggleFavorite = viewModel::toggleFavorite,
                    onCreateCustomMeal = viewModel::createCustomMeal,
                    onCollectionSelected = { collectionId ->
                        viewModel.selectCollection(collectionId)
                        navController.navigate("${CookMateRoutes.COLLECTION}/$collectionId")
                    },
                    onCreateCollection = viewModel::createCollection,
                    onDeleteCollection = viewModel::deleteCollection,
                    onToggleCollectionPin = viewModel::toggleCollectionPin
                )
            }

            composable(CookMateRoutes.RECENT) {
                RecentScreen(
                    uiState = uiState,
                    onMealSelected = { mealId ->
                        navController.navigate("${CookMateRoutes.DETAIL}/$mealId")
                    },
                    onToggleFavorite = viewModel::toggleFavorite
                )
            }

            composable(CookMateRoutes.SHOPPING) {
                ShoppingListScreen(
                    uiState = uiState,
                    onAddManualItem = viewModel::addManualShoppingItem,
                    onSetChecked = viewModel::setShoppingItemChecked,
                    onRemoveItem = viewModel::removeShoppingItem,
                    onClearChecked = viewModel::clearCheckedShoppingItems,
                    onClearAll = viewModel::clearAllShoppingItems
                )
            }

            composable(CookMateRoutes.SETTINGS) {
                SettingsScreen(
                    uiState = uiState,
                    onOfflineOnlyChange = viewModel::setOfflineOnlyMode,
                    onBackgroundSyncChange = viewModel::setBackgroundSyncEnabled,
                    onHistoryLimitChange = viewModel::setHistoryLimit,
                    onStartDestinationChange = viewModel::setStartDestination,
                    onSyncNow = viewModel::syncSavedMealsNow,
                    onClearMessage = viewModel::clearSyncStatusMessage
                )
            }

            composable("${CookMateRoutes.DETAIL}/{mealId}") { backStackEntry ->
                val mealId = backStackEntry.arguments?.getString("mealId") ?: return@composable
                LaunchedEffect(mealId) {
                    viewModel.selectMealForDetail(mealId)
                }
                DetailScreen(
                    uiState = uiState,
                    onBackClick = { navController.popBackStack() },
                    onToggleFavorite = viewModel::toggleFavorite,
                    onRetry = viewModel::selectMealForDetail,
                    onClearDetail = viewModel::clearDetail,
                    onSaveNote = viewModel::saveMealNote,
                    onAddIngredientsToShoppingList = viewModel::addMealIngredientsToShoppingList,
                    onToggleMealInCollection = viewModel::toggleMealInCollection,
                    onUpdateCustomMeal = viewModel::updateCustomMeal,
                    onDeleteCustomMeal = { localMealId ->
                        viewModel.deleteCustomMeal(localMealId)
                        navController.popBackStack()
                    }
                )
            }

            composable("${CookMateRoutes.COLLECTION}/{collectionId}") { backStackEntry ->
                val collectionId = backStackEntry.arguments?.getString("collectionId")?.toLongOrNull()
                    ?: return@composable
                LaunchedEffect(collectionId) {
                    viewModel.selectCollection(collectionId)
                }
                CollectionDetailScreen(
                    uiState = uiState,
                    onBackClick = {
                        viewModel.clearSelectedCollection()
                        navController.popBackStack()
                    },
                    onMealSelected = { mealId ->
                        navController.navigate("${CookMateRoutes.DETAIL}/$mealId")
                    },
                    onToggleFavorite = viewModel::toggleFavorite,
                    onRemoveMeal = viewModel::removeMealFromCollection,
                    onUpdateCollection = viewModel::updateCollection
                )
            }
        }
    }
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
