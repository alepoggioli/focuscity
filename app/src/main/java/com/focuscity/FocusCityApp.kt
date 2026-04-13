package com.focuscity

import android.app.Application
import com.focuscity.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.focuscity.data.db.AppDatabase
import com.focuscity.data.repository.AppRepository

class FocusCityApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Create notification channels
        NotificationHelper.createChannels(this)

        // Schedule daily reminder (default 6 PM)
        NotificationHelper.scheduleDailyReminder(this, hour = 18, minute = 0)

        // Ensure data is initialized
        appScope.launch {
            val db = AppDatabase.getInstance(this@FocusCityApp)
            val repo = AppRepository(db)
            repo.ensureUserProfileExists()
            repo.initializeCity()
        }
    }
}
