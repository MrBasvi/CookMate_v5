package com.example.cookmate.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.cookmate.data.db.converter.IngredientConverter
import com.example.cookmate.data.db.dao.CachedMealDao
import com.example.cookmate.data.db.dao.FavouriteMealDao
import com.example.cookmate.data.db.dao.MealCollectionDao
import com.example.cookmate.data.db.dao.MealNoteDao
import com.example.cookmate.data.db.dao.RecentMealDao
import com.example.cookmate.data.db.dao.ShoppingListDao
import com.example.cookmate.data.db.entity.CachedMealEntity
import com.example.cookmate.data.db.entity.CollectionMealCrossRef
import com.example.cookmate.data.db.entity.FavouriteMealEntity
import com.example.cookmate.data.db.entity.MealCollectionEntity
import com.example.cookmate.data.db.entity.MealNoteEntity
import com.example.cookmate.data.db.entity.RecentMealEntity
import com.example.cookmate.data.db.entity.ShoppingListItemEntity

@Database(
    entities = [
        FavouriteMealEntity::class,
        CachedMealEntity::class,
        RecentMealEntity::class,
        MealCollectionEntity::class,
        CollectionMealCrossRef::class,
        MealNoteEntity::class,
        ShoppingListItemEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(IngredientConverter::class)
abstract class CookMateDatabase : RoomDatabase() {
    abstract fun favouriteMealDao(): FavouriteMealDao
    abstract fun cachedMealDao(): CachedMealDao
    abstract fun recentMealDao(): RecentMealDao
    abstract fun mealCollectionDao(): MealCollectionDao
    abstract fun mealNoteDao(): MealNoteDao
    abstract fun shoppingListDao(): ShoppingListDao
}
