package com.example.cookmate.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

class SavedMealsSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "saved_meals_sync"
    }

    override suspend fun doWork(): Result {
        val entryPoint = SyncWorkerEntryPoint.from(applicationContext)
        val historyLimit = entryPoint.preferencesManager().historyLimitFlow.first()

        return try {
            val result = entryPoint.savedMealsSyncUseCase().sync(historyLimit)
            if (result.failedMealIds.isEmpty()) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (throwable: CancellationException) {
            throw throwable
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
