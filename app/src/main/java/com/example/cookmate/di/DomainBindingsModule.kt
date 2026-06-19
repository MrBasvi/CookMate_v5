package com.example.cookmate.di

import com.example.cookmate.data.repository.MealRepository
import com.example.cookmate.data.service.OfflineMealService
import com.example.cookmate.domain.MealDetailsFetcher
import com.example.cookmate.domain.SyncLocalDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainBindingsModule {

    @Binds
    abstract fun bindMealDetailsFetcher(
        repository: MealRepository
    ): MealDetailsFetcher

    @Binds
    abstract fun bindSyncLocalDataSource(
        offlineMealService: OfflineMealService
    ): SyncLocalDataSource
}
