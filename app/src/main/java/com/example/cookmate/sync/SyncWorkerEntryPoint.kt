package com.example.cookmate.sync

import android.content.Context
import com.example.cookmate.data.preferences.PreferencesManager
import com.example.cookmate.domain.SavedMealsSyncUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SyncWorkerEntryPoint {
    fun savedMealsSyncUseCase(): SavedMealsSyncUseCase
    fun preferencesManager(): PreferencesManager

    companion object {
        fun from(context: Context): SyncWorkerEntryPoint =
            EntryPointAccessors.fromApplication(context, SyncWorkerEntryPoint::class.java)
    }
}
