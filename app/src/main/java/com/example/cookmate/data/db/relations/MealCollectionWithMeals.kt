package com.example.cookmate.data.db.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.cookmate.data.db.entity.CachedMealEntity
import com.example.cookmate.data.db.entity.CollectionMealCrossRef
import com.example.cookmate.data.db.entity.MealCollectionEntity

data class MealCollectionWithMeals(
    @Embedded
    val collection: MealCollectionEntity,
    @Relation(
        parentColumn = "collectionId",
        entityColumn = "idMeal",
        associateBy = Junction(
            value = CollectionMealCrossRef::class,
            parentColumn = "collectionId",
            entityColumn = "mealId"
        )
    )
    val meals: List<CachedMealEntity>
)
