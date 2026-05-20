package com.example.cookmate.di

import android.content.Context
import androidx.room.Room
import com.example.cookmate.data.db.dao.CachedMealDao
import com.example.cookmate.data.db.CookMateDatabase
import com.example.cookmate.data.db.dao.FavouriteMealDao
import com.example.cookmate.data.db.dao.MealCollectionDao
import com.example.cookmate.data.db.dao.MealNoteDao
import com.example.cookmate.data.db.dao.RecentMealDao
import com.example.cookmate.data.db.dao.ShoppingListDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): CookMateDatabase = Room.databaseBuilder(
        context,
        CookMateDatabase::class.java,
        "cook_mate_database"
    ).build()
    
    @Singleton
    @Provides
    fun provideFavouriteMealDao(database: CookMateDatabase): FavouriteMealDao =
        database.favouriteMealDao()

    @Singleton
    @Provides
    fun provideCachedMealDao(database: CookMateDatabase): CachedMealDao =
        database.cachedMealDao()

    @Singleton
    @Provides
    fun provideRecentMealDao(database: CookMateDatabase): RecentMealDao =
        database.recentMealDao()

    @Singleton
    @Provides
    fun provideMealCollectionDao(database: CookMateDatabase): MealCollectionDao =
        database.mealCollectionDao()

    @Singleton
    @Provides
    fun provideMealNoteDao(database: CookMateDatabase): MealNoteDao =
        database.mealNoteDao()

    @Singleton
    @Provides
    fun provideShoppingListDao(database: CookMateDatabase): ShoppingListDao =
        database.shoppingListDao()
}
