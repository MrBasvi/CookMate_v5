package com.example.cookmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cookmate.data.model.Meal
import com.example.cookmate.data.preferences.PreferencesManager
import com.example.cookmate.data.repository.MealRepository
import com.example.cookmate.data.service.FavouriteMealService
import com.example.cookmate.data.service.MealCollectionService
import com.example.cookmate.data.service.MealNoteService
import com.example.cookmate.data.service.OfflineMealService
import com.example.cookmate.data.service.ShoppingListService
import com.example.cookmate.domain.CustomMealDraft
import com.example.cookmate.domain.CustomMealEditor
import com.example.cookmate.domain.DiscoverFeedReducer
import com.example.cookmate.domain.RemoteSearchState
import com.example.cookmate.domain.SavedMealsSyncUseCase
import com.example.cookmate.sync.SyncScheduler
import com.example.cookmate.ui.state.CookMateUiState
import com.example.cookmate.ui.state.MealDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class CookMateViewModel @Inject constructor(
    private val repository: MealRepository,
    private val favouriteService: FavouriteMealService,
    private val offlineMealService: OfflineMealService,
    private val mealCollectionService: MealCollectionService,
    private val mealNoteService: MealNoteService,
    private val shoppingListService: ShoppingListService,
    private val preferencesManager: PreferencesManager,
    private val discoverFeedReducer: DiscoverFeedReducer,
    private val savedMealsSyncUseCase: SavedMealsSyncUseCase,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedMealId = MutableStateFlow<String?>(null)
    private val _selectedCollectionId = MutableStateFlow<Long?>(null)
    private val _mealDetailState = MutableStateFlow<MealDetailUiState>(MealDetailUiState.Loading)
    private val _refreshRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val _syncStatusMessage = MutableStateFlow<String?>(null)

    private val normalizedQuery = _searchQuery
        .map { it.trim() }
        .debounce(350)
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val favoriteMeals = favouriteService.getAllFavourites()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val favoriteIds = favoriteMeals
        .map { meals -> meals.map { it.idMeal } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val showOnlyFavorites = preferencesManager.showOnlyFavoritesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val offlineOnlyMode = preferencesManager.offlineOnlyModeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val backgroundSyncEnabled = preferencesManager.backgroundSyncEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val historyLimit = preferencesManager.historyLimitFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 30)

    private val startDestination = preferencesManager.startDestinationFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val recentMeals = historyLimit
        .flatMapLatest { limit -> offlineMealService.observeRecentMeals(limit) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val collections = mealCollectionService.observeCollections()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val mealNotes = mealNoteService.observeAllNotes()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val localMeals = offlineMealService.observeLocalMeals()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val shoppingListItems = shoppingListService.observeItems()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val cachedSearchMatches = normalizedQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                offlineMealService.searchCachedMeals(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val remoteSearchState = combine(
        normalizedQuery,
        offlineOnlyMode,
        _refreshRequests.onStart { emit(Unit) }
    ) { query, offlineOnly, _ ->
        SearchRequest(query = query, offlineOnly = offlineOnly)
    }.flatMapLatest { request ->
        when {
            request.query.isBlank() -> flowOf(RemoteSearchState.Idle)
            request.offlineOnly -> flowOf(RemoteSearchState.Idle)
            else -> flow {
                val meals = repository.searchMealsByName(request.query)
                offlineMealService.cacheMeals(meals)
                emit(if (meals.isEmpty()) RemoteSearchState.Empty else RemoteSearchState.Idle)
            }.onStart {
                emit(RemoteSearchState.Loading)
            }
        }
    }.catch { throwable ->
        if (throwable is CancellationException) throw throwable
        emit(RemoteSearchState.Error(throwable.localizedMessage ?: "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0432\u044b\u043f\u043e\u043b\u043d\u0438\u0442\u044c \u043f\u043e\u0438\u0441\u043a"))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, RemoteSearchState.Idle)

    private val selectedMealNote = _selectedMealId
        .flatMapLatest { mealId ->
            if (mealId == null) flowOf(null) else mealNoteService.observeMealNote(mealId)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val selectedMealMemberships = _selectedMealId
        .flatMapLatest { mealId ->
            if (mealId == null) flowOf(emptyList()) else mealCollectionService.observeMemberships(mealId)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val selectedCollection = _selectedCollectionId
        .flatMapLatest { collectionId ->
            if (collectionId == null) flowOf(null) else mealCollectionService.observeCollection(collectionId)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val uiState: StateFlow<CookMateUiState> = combine(
        _searchQuery,
        cachedSearchMatches,
        recentMeals,
        favoriteIds,
        favoriteMeals,
        localMeals,
        showOnlyFavorites,
        remoteSearchState,
        _selectedMealId,
        _mealDetailState,
        collections,
        _selectedCollectionId,
        selectedCollection,
        selectedMealMemberships,
        shoppingListItems,
        mealNotes,
        selectedMealNote,
        offlineOnlyMode,
        backgroundSyncEnabled,
        historyLimit,
        startDestination,
        _syncStatusMessage
    ) { values ->
        val query = values[0] as String
        val normalizedQuery = query.trim()
        val cachedMatches = values[1] as List<Meal>
        val recent = values[2] as List<com.example.cookmate.data.model.RecentMeal>
        val favorites = values[3] as List<String>
        val favoriteMealsValue = values[4] as List<Meal>
        val localMealsValue = values[5] as List<Meal>
        val savedOnly = values[6] as Boolean
        val searchState = values[7] as RemoteSearchState
        val selectedMealId = values[8] as String?
        val mealDetailState = values[9] as MealDetailUiState
        val collectionList = values[10] as List<com.example.cookmate.data.model.MealCollectionSummary>
        val selectedCollectionId = values[11] as Long?
        val selectedCollectionValue = values[12] as com.example.cookmate.data.model.MealCollectionDetail?
        val memberships = values[13] as List<com.example.cookmate.data.model.MealCollectionMembership>
        val shoppingItems = values[14] as List<com.example.cookmate.data.model.ShoppingListItem>
        val notes = values[15] as Map<String, com.example.cookmate.data.model.MealNote>
        val selectedNote = values[16] as com.example.cookmate.data.model.MealNote?
        val offlineOnly = values[17] as Boolean
        val syncEnabled = values[18] as Boolean
        val historyLimitValue = values[19] as Int
        val startDestinationValue = values[20] as String
        val syncStatus = values[21] as String?

        val listState = discoverFeedReducer.reduce(
            query = normalizedQuery,
            cachedMatches = cachedMatches,
            recentMeals = recent.map { it.meal },
            favoriteIds = favorites.toSet(),
            showOnlyFavorites = savedOnly,
            remoteState = searchState
        )

        val detailMeal = (mealDetailState as? MealDetailUiState.Success)?.meal
        val collectionMeals = selectedCollectionValue?.meals.orEmpty()
        val allMeals = (
            cachedMatches +
                recent.map { it.meal } +
                favoriteMealsValue +
                localMealsValue +
                collectionMeals +
                listOfNotNull(detailMeal)
            ).distinctBy { it.idMeal }

        CookMateUiState(
            searchQuery = query,
            mealListState = listState,
            mealDetailState = if (selectedMealId == null) null else mealDetailState,
            selectedMealId = selectedMealId,
            selectedCollectionId = selectedCollectionId,
            favorites = favorites,
            favoriteMeals = favoriteMealsValue,
            localMeals = localMealsValue,
            allMeals = allMeals,
            showOnlyFavorites = savedOnly,
            collections = collectionList,
            selectedCollection = selectedCollectionValue,
            selectedMealMemberships = memberships,
            recentMeals = recent,
            shoppingListItems = shoppingItems,
            mealNotes = notes,
            selectedMealNote = selectedNote,
            offlineOnlyMode = offlineOnly,
            backgroundSyncEnabled = syncEnabled,
            historyLimit = historyLimitValue,
            startDestination = startDestinationValue,
            syncStatusMessage = syncStatus
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CookMateUiState())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun retrySearch() {
        _refreshRequests.tryEmit(Unit)
    }

    fun selectMealForDetail(mealId: String) {
        _selectedMealId.value = mealId
        _mealDetailState.value = MealDetailUiState.Loading

        viewModelScope.launch {
            val cachedMeal = offlineMealService.getCachedMeal(mealId)
            if (cachedMeal != null) {
                _mealDetailState.value = MealDetailUiState.Success(cachedMeal)
                offlineMealService.markViewed(cachedMeal, historyLimit.value)
            }

            if (offlineOnlyMode.value) {
                if (cachedMeal == null) {
                    _mealDetailState.value = MealDetailUiState.Error("\u042d\u0442\u043e\u0442 \u0440\u0435\u0446\u0435\u043f\u0442 \u0435\u0449\u0451 \u043d\u0435 \u0441\u043e\u0445\u0440\u0430\u043d\u0451\u043d \u0434\u043b\u044f \u043e\u0444\u043b\u0430\u0439\u043d-\u0440\u0435\u0436\u0438\u043c\u0430")
                }
                return@launch
            }

            try {
                val remoteMeal = repository.getMealDetails(mealId)
                offlineMealService.cacheMeal(remoteMeal)
                offlineMealService.markViewed(remoteMeal, historyLimit.value)
                _mealDetailState.value = MealDetailUiState.Success(remoteMeal)
            } catch (throwable: CancellationException) {
                throw throwable
            } catch (throwable: Exception) {
                if (cachedMeal == null) {
                    _mealDetailState.value = MealDetailUiState.Error(
                        throwable.localizedMessage ?: "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c \u0440\u0435\u0446\u0435\u043f\u0442"
                    )
                }
            }
        }
    }

    fun clearDetail() {
        _selectedMealId.value = null
        _mealDetailState.value = MealDetailUiState.Loading
    }

    fun toggleFavorite(mealId: String) {
        viewModelScope.launch {
            try {
                if (favouriteService.isFavourite(mealId)) {
                    favouriteService.removeFavourite(mealId)
                } else {
                    val meal = resolveMeal(mealId)
                    favouriteService.addFavourite(meal)
                    offlineMealService.cacheMeal(meal)
                }
            } catch (throwable: CancellationException) {
                throw throwable
            } catch (throwable: Exception) {
                _syncStatusMessage.value = throwable.localizedMessage ?: "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u0431\u043d\u043e\u0432\u0438\u0442\u044c \u0438\u0437\u0431\u0440\u0430\u043d\u043d\u043e\u0435"
            }
        }
    }

    fun toggleFavorite(meal: Meal) {
        toggleFavorite(meal.idMeal)
    }

    fun saveMealNote(mealId: String, noteText: String, rating: Int) {
        viewModelScope.launch {
            try {
                mealNoteService.saveNote(mealId, noteText, rating)
                _syncStatusMessage.value = "\u0417\u0430\u043c\u0435\u0442\u043a\u0430 \u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u0430"
            } catch (throwable: CancellationException) {
                throw throwable
            } catch (throwable: Exception) {
                _syncStatusMessage.value = throwable.localizedMessage ?: "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0437\u0430\u043c\u0435\u0442\u043a\u0443"
            }
        }
    }

    fun createCollection(title: String, description: String) {
        viewModelScope.launch {
            try {
                val collectionId = mealCollectionService.createCollection(title, description)
                _selectedCollectionId.value = collectionId
                _syncStatusMessage.value = "\u041a\u043e\u043b\u043b\u0435\u043a\u0446\u0438\u044f \u0441\u043e\u0437\u0434\u0430\u043d\u0430"
            } catch (throwable: CancellationException) {
                throw throwable
            } catch (throwable: Exception) {
                _syncStatusMessage.value = throwable.localizedMessage ?: "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043e\u0437\u0434\u0430\u0442\u044c \u043a\u043e\u043b\u043b\u0435\u043a\u0446\u0438\u044e"
            }
        }
    }

    fun updateCollection(collectionId: Long, title: String, description: String) {
        viewModelScope.launch {
            try {
                mealCollectionService.updateCollection(collectionId, title, description)
                _syncStatusMessage.value = "Коллекция обновлена"
            } catch (throwable: IllegalArgumentException) {
                _syncStatusMessage.value = throwable.message ?: "Проверьте данные коллекции"
            } catch (throwable: CancellationException) {
                throw throwable
            } catch (throwable: Exception) {
                _syncStatusMessage.value = throwable.localizedMessage ?: "Не удалось обновить коллекцию"
            }
        }
    }

    fun createCustomMeal(
        title: String,
        category: String,
        area: String,
        instructions: String,
        ingredientsText: String,
        imageUrl: String
    ) {
        viewModelScope.launch {
            try {
                val meal = CustomMealEditor.createMeal(
                    idProvider = { "local-${UUID.randomUUID()}" },
                    draft = CustomMealDraft(
                        title = title,
                        category = category,
                        area = area,
                        instructions = instructions,
                        ingredientsText = ingredientsText,
                        imageUrl = imageUrl
                    )
                )
                offlineMealService.cacheMeal(meal)
                _syncStatusMessage.value = "Свой рецепт сохранён"
            } catch (throwable: IllegalArgumentException) {
                _syncStatusMessage.value = throwable.message ?: "Проверьте данные рецепта"
            } catch (throwable: CancellationException) {
                throw throwable
            } catch (throwable: Exception) {
                _syncStatusMessage.value = throwable.localizedMessage ?: "Не удалось сохранить свой рецепт"
            }
        }
    }
    fun updateCustomMeal(
        mealId: String,
        title: String,
        category: String,
        area: String,
        instructions: String,
        ingredientsText: String,
        imageUrl: String
    ) {
        if (!isLocalMealId(mealId)) {
            _syncStatusMessage.value = "Редактировать можно только свои рецепты"
            return
        }
        viewModelScope.launch {
            try {
                val currentMeal = resolveMeal(mealId)
                val updatedMeal = CustomMealEditor.updateMeal(
                    existing = currentMeal,
                    draft = CustomMealDraft(
                        title = title,
                        category = category,
                        area = area,
                        instructions = instructions,
                        ingredientsText = ingredientsText,
                        imageUrl = imageUrl
                    )
                )
                offlineMealService.cacheMeal(updatedMeal)
                if (favouriteService.isFavourite(mealId)) {
                    favouriteService.addFavourite(updatedMeal)
                }
                if (_selectedMealId.value == mealId) {
                    _mealDetailState.value = MealDetailUiState.Success(updatedMeal)
                }
                _syncStatusMessage.value = "Свой рецепт обновлён"
            } catch (throwable: IllegalArgumentException) {
                _syncStatusMessage.value = throwable.message ?: "Проверьте данные рецепта"
            } catch (throwable: CancellationException) {
                throw throwable
            } catch (throwable: Exception) {
                _syncStatusMessage.value = throwable.localizedMessage ?: "Не удалось обновить рецепт"
            }
        }
    }
    fun deleteCustomMeal(mealId: String) {
        if (!isLocalMealId(mealId)) {
            _syncStatusMessage.value = "\u0423\u0434\u0430\u043b\u044f\u0442\u044c \u043c\u043e\u0436\u043d\u043e \u0442\u043e\u043b\u044c\u043a\u043e \u0441\u0432\u043e\u0438 \u0440\u0435\u0446\u0435\u043f\u0442\u044b"
            return
        }

        viewModelScope.launch {
            try {
                favouriteService.removeFavourite(mealId)
                mealNoteService.deleteNote(mealId)
                mealCollectionService.removeMealFromAllCollections(mealId)
                offlineMealService.deleteCachedMeal(mealId)

                if (_selectedMealId.value == mealId) {
                    clearDetail()
                }

                _syncStatusMessage.value = "\u0421\u0432\u043e\u0439 \u0440\u0435\u0446\u0435\u043f\u0442 \u0443\u0434\u0430\u043b\u0451\u043d"
            } catch (throwable: CancellationException) {
                throw throwable
            } catch (throwable: Exception) {
                _syncStatusMessage.value = throwable.localizedMessage ?: "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u0440\u0435\u0446\u0435\u043f\u0442"
            }
        }
    }

    fun selectCollection(collectionId: Long) {
        _selectedCollectionId.value = collectionId
    }

    fun clearSelectedCollection() {
        _selectedCollectionId.value = null
    }

    fun deleteCollection(collectionId: Long) {
        viewModelScope.launch {
            try {
                mealCollectionService.deleteCollection(collectionId)
                if (_selectedCollectionId.value == collectionId) {
                    _selectedCollectionId.value = null
                }
                _syncStatusMessage.value = "\u041a\u043e\u043b\u043b\u0435\u043a\u0446\u0438\u044f \u0443\u0434\u0430\u043b\u0435\u043d\u0430"
            } catch (throwable: CancellationException) {
                throw throwable
            } catch (throwable: Exception) {
                _syncStatusMessage.value = throwable.localizedMessage ?: "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u043a\u043e\u043b\u043b\u0435\u043a\u0446\u0438\u044e"
            }
        }
    }

    fun toggleCollectionPin(collectionId: Long) {
        viewModelScope.launch {
            try {
                mealCollectionService.togglePinned(collectionId)
            } catch (throwable: CancellationException) {
                throw throwable
            } catch (throwable: Exception) {
                _syncStatusMessage.value = throwable.localizedMessage ?: "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u0431\u043d\u043e\u0432\u0438\u0442\u044c \u043a\u043e\u043b\u043b\u0435\u043a\u0446\u0438\u044e"
            }
        }
    }

    fun toggleMealInCollection(collectionId: Long, mealId: String) {
        viewModelScope.launch {
            try {
                val membership = uiState.value.selectedMealMemberships.firstOrNull { it.collectionId == collectionId }
                if (membership?.containsMeal == true) {
                    mealCollectionService.removeMealFromCollection(collectionId, mealId)
                    _syncStatusMessage.value = "\u0420\u0435\u0446\u0435\u043f\u0442 \u0443\u0431\u0440\u0430\u043d \u0438\u0437 \u043a\u043e\u043b\u043b\u0435\u043a\u0446\u0438\u0438"
                } else {
                    val meal = resolveMeal(mealId)
                    mealCollectionService.addMealToCollection(collectionId, meal)
                    _syncStatusMessage.value = "\u0420\u0435\u0446\u0435\u043f\u0442 \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d \u0432 \u043a\u043e\u043b\u043b\u0435\u043a\u0446\u0438\u044e"
                }
            } catch (throwable: CancellationException) {
                throw throwable
            } catch (throwable: Exception) {
                _syncStatusMessage.value = throwable.localizedMessage ?: "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u0431\u043d\u043e\u0432\u0438\u0442\u044c \u0441\u043e\u0441\u0442\u0430\u0432 \u043a\u043e\u043b\u043b\u0435\u043a\u0446\u0438\u0438"
            }
        }
    }

    fun removeMealFromCollection(collectionId: Long, mealId: String) {
        viewModelScope.launch {
            try {
                mealCollectionService.removeMealFromCollection(collectionId, mealId)
            } catch (throwable: CancellationException) {
                throw throwable
            } catch (throwable: Exception) {
                _syncStatusMessage.value = throwable.localizedMessage ?: "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0443\u0431\u0440\u0430\u0442\u044c \u0440\u0435\u0446\u0435\u043f\u0442"
            }
        }
    }

    fun addMealIngredientsToShoppingList(mealId: String) {
        viewModelScope.launch {
            try {
                val meal = resolveMeal(mealId)
                val affectedCount = shoppingListService.addIngredientsFromMeal(meal)
                _syncStatusMessage.value = if (affectedCount == 0) {
                    "В рецепте нет ингредиентов для списка покупок"
                } else {
                    "В список покупок добавлено позиций: $affectedCount"
                }
            } catch (throwable: CancellationException) {
                throw throwable
            } catch (throwable: Exception) {
                _syncStatusMessage.value = throwable.localizedMessage ?: "Не удалось добавить ингредиенты"
            }
        }
    }

    fun addManualShoppingItem(name: String, measure: String) {
        viewModelScope.launch {
            try {
                val added = shoppingListService.addManualItem(name, measure)
                _syncStatusMessage.value = if (added) {
                    "Позиция добавлена в список покупок"
                } else {
                    "Название позиции не может быть пустым"
                }
            } catch (throwable: CancellationException) {
                throw throwable
            } catch (throwable: Exception) {
                _syncStatusMessage.value = throwable.localizedMessage ?: "Не удалось добавить позицию"
            }
        }
    }

    fun setShoppingItemChecked(itemId: Long, checked: Boolean) {
        viewModelScope.launch {
            try {
                val item = uiState.value.shoppingListItems.firstOrNull { it.itemId == itemId } ?: return@launch
                shoppingListService.setChecked(item, checked)
            } catch (throwable: CancellationException) {
                throw throwable
            } catch (throwable: Exception) {
                _syncStatusMessage.value = throwable.localizedMessage ?: "Не удалось обновить список покупок"
            }
        }
    }

    fun removeShoppingItem(itemId: Long) {
        viewModelScope.launch {
            try {
                shoppingListService.removeItem(itemId)
            } catch (throwable: CancellationException) {
                throw throwable
            } catch (throwable: Exception) {
                _syncStatusMessage.value = throwable.localizedMessage ?: "Не удалось удалить позицию"
            }
        }
    }

    fun clearCheckedShoppingItems() {
        viewModelScope.launch {
            try {
                shoppingListService.clearCheckedItems()
                _syncStatusMessage.value = "Купленные позиции очищены"
            } catch (throwable: CancellationException) {
                throw throwable
            } catch (throwable: Exception) {
                _syncStatusMessage.value = throwable.localizedMessage ?: "Не удалось очистить список покупок"
            }
        }
    }

    fun clearAllShoppingItems() {
        viewModelScope.launch {
            try {
                shoppingListService.clearAllItems()
                _syncStatusMessage.value = "Список покупок полностью очищен"
            } catch (throwable: CancellationException) {
                throw throwable
            } catch (throwable: Exception) {
                _syncStatusMessage.value = throwable.localizedMessage ?: "Не удалось очистить список покупок"
            }
        }
    }

    fun setShowOnlyFavorites(show: Boolean) {
        viewModelScope.launch {
            preferencesManager.setShowOnlyFavorites(show)
        }
    }

    fun setOfflineOnlyMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setOfflineOnlyMode(enabled)
            _refreshRequests.tryEmit(Unit)
        }
    }

    fun setBackgroundSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setBackgroundSyncEnabled(enabled)
            if (enabled) {
                syncScheduler.scheduleBackgroundSync()
            } else {
                syncScheduler.cancelBackgroundSync()
            }
        }
    }

    fun setHistoryLimit(limit: Int) {
        viewModelScope.launch {
            preferencesManager.setHistoryLimit(limit)
        }
    }

    fun setStartDestination(route: String) {
        viewModelScope.launch {
            preferencesManager.setStartDestination(route)
        }
    }

    fun clearSyncStatusMessage() {
        _syncStatusMessage.value = null
    }

    fun syncSavedMealsNow() {
        viewModelScope.launch {
            try {
                _syncStatusMessage.value = "\u0418\u0434\u0451\u0442 \u0441\u0438\u043d\u0445\u0440\u043e\u043d\u0438\u0437\u0430\u0446\u0438\u044f..."
                val result = savedMealsSyncUseCase.sync(historyLimit.value)
                _syncStatusMessage.value = if (result.failedMealIds.isEmpty()) {
                    "\u0421\u0438\u043d\u0445\u0440\u043e\u043d\u0438\u0437\u0438\u0440\u043e\u0432\u0430\u043d\u043e \u0440\u0435\u0446\u0435\u043f\u0442\u043e\u0432: ${result.syncedCount}"
                } else {
                    "\u0421\u0438\u043d\u0445\u0440\u043e\u043d\u0438\u0437\u0438\u0440\u043e\u0432\u0430\u043d\u043e ${result.syncedCount} \u0438\u0437 ${result.requestedCount}"
                }
            } catch (throwable: CancellationException) {
                throw throwable
            } catch (throwable: Exception) {
                _syncStatusMessage.value = throwable.localizedMessage ?: "\u0421\u0438\u043d\u0445\u0440\u043e\u043d\u0438\u0437\u0430\u0446\u0438\u044f \u043d\u0435 \u0443\u0434\u0430\u043b\u0430\u0441\u044c"
            }
        }
    }

    private suspend fun resolveMeal(mealId: String): Meal {
        return uiState.value.allMeals.firstOrNull { it.idMeal == mealId }
            ?: offlineMealService.getCachedMeal(mealId)
            ?: repository.getMealDetails(mealId)
    }

    private fun isLocalMealId(mealId: String): Boolean = mealId.startsWith("local-")
    private data class SearchRequest(
        val query: String,
        val offlineOnly: Boolean
    )
}


