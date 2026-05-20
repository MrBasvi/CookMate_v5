package com.example.cookmate.sync

import android.content.Context
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val workManager: WorkManager
        get() = WorkManager.getInstance(context)

    fun scheduleBackgroundSync() {
        val request = PeriodicWorkRequestBuilder<SavedMealsSyncWorker>(6, TimeUnit.HOURS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            SavedMealsSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelBackgroundSync() {
        workManager.cancelUniqueWork(SavedMealsSyncWorker.WORK_NAME)
    }
}
