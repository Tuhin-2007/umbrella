package com.example.umbrellaalert

import android.app.Application
import com.example.umbrellaalert.notification.NotificationHelper

class UmbrellaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize notification channel at application startup
        NotificationHelper.createNotificationChannel(this)
    }
}
