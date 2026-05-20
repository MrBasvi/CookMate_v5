package com.example.cookmate.data.service

import com.example.cookmate.data.db.dao.MealCollectionDao
import com.example.cookmate.data.db.entity.CollectionMealCrossRef
import com.example.cookmate.data.db.entity.MealCollectionEntity
import com.example.cookmate.data.db.relations.MealCollectionMembershipEntity
import com.example.cookmate.data.db.relations.MealCollectionSummaryEntity
import com.example.cookmate.data.db.relations.MealCollectionWithMeals
import com.example.cookmate.data.model.Meal
import com.example.cookmate.data.model.MealCollectionDetail
import com.example.cookmate.data.model.MealCollectionMembership
import com.example.cookmate.data.model.MealCollectionSummary
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class MealCollectionService @Inject constructor(
    private val mealCollectionDao: MealCollectionDao,
    private val offlineMealService: OfflineMealService
) {

    init {
        offlineMealService.setCollectionIdProvider { mealCollectionDao.getAllCollectionMealIds() }
    }

    fun observeCollections(): Flow<List<MealCollectionSummary>> =
        mealCollectionDao.observeCollectionSummaries().map { summaries ->
            summaries.map { it.toModel() }
        }

    fun observeCollection(collectionId: Long): Flow<MealCollectionDetail?> =
        mealCollectionDao.observeCollection(collectionId).map { relation ->
            relation?.toModel()
        }

    fun observeMemberships(mealId: String): Flow<List<MealCollectionMembership>> =
        mealCollectionDao.observeMemberships(mealId).map { memberships ->
            memberships.map { it.toModel() }
        }

    suspend fun createCollection(title: String, description: String): Long {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            throw IllegalArgumentException("Collection title cannot be empty")
        }

        return mealCollectionDao.upsertCollection(
            MealCollectionEntity(
                title = trimmedTitle,
                description = description.trim()
            )
        )
    }

    suspend fun updateCollection(collectionId: Long, title: String, description: String) {
        val current = mealCollectionDao.getCollection(collectionId)
            ?: throw IllegalArgumentException("Collection does not exist")
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            throw IllegalArgumentException("Collection title cannot be empty")
        }

        mealCollectionDao.upsertCollection(
            current.copy(
                title = trimmedTitle,
                description = description.trim(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteCollection(collectionId: Long) {
        mealCollectionDao.deleteMealsForCollection(collectionId)
        mealCollectionDao.deleteCollection(collectionId)
    }

    suspend fun togglePinned(collectionId: Long) {
        val current = mealCollectionDao.getCollection(collectionId) ?: return
        mealCollectionDao.upsertCollection(
            current.copy(
                isPinned = !current.isPinned,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun addMealToCollection(collectionId: Long, meal: Meal) {
        offlineMealService.cacheMeal(meal)
        mealCollectionDao.addMealToCollection(
            CollectionMealCrossRef(collectionId = collectionId, mealId = meal.idMeal)
        )
        touchCollection(collectionId)
    }

    suspend fun removeMealFromCollection(collectionId: Long, mealId: String) {
        mealCollectionDao.removeMealFromCollection(collectionId, mealId)
        touchCollection(collectionId)
    }

    suspend fun removeMealFromAllCollections(mealId: String) {
        mealCollectionDao.removeMealFromAllCollections(mealId)
    }

    private suspend fun touchCollection(collectionId: Long) {
        val current = mealCollectionDao.getCollection(collectionId) ?: return
        mealCollectionDao.upsertCollection(current.copy(updatedAt = System.currentTimeMillis()))
    }

    private fun MealCollectionSummaryEntity.toModel() = MealCollectionSummary(
        collectionId = collectionId,
        title = title,
        description = description,
        mealCount = mealCount,
        isPinned = isPinned,
        updatedAt = updatedAt
    )

    private fun MealCollectionMembershipEntity.toModel() = MealCollectionMembership(
        collectionId = collectionId,
        title = title,
        isPinned = isPinned,
        containsMeal = containsMeal
    )

    private fun MealCollectionWithMeals.toModel() = MealCollectionDetail(
        collectionId = collection.collectionId,
        title = collection.title,
        description = collection.description,
        isPinned = collection.isPinned,
        meals = meals.map { cached ->
            Meal(
                idMeal = cached.idMeal,
                strMeal = cached.strMeal,
                strCategory = cached.strCategory,
                strArea = cached.strArea,
                strInstructions = cached.strInstructions,
                strMealThumb = cached.strMealThumb,
                ingredients = cached.ingredients
            )
        },
        updatedAt = collection.updatedAt
    )
}
