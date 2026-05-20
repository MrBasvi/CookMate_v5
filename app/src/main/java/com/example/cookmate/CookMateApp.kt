package com.example.cookmate

import android.app.Application
import com.example.cookmate.data.preferences.PreferencesManager
import com.example.cookmate.sync.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltAndroidApp
class CookMateApp : Application() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var syncScheduler: SyncScheduler

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.Default).launch {
            val backgroundSyncEnabled = preferencesManager.backgroundSyncEnabledFlow.first()
            if (backgroundSyncEnabled) {
                syncScheduler.scheduleBackgroundSync()
            } else {
                syncScheduler.cancelBackgroundSync()
            }
        }
    }
}
