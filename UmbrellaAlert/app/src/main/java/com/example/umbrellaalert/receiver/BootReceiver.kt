package com.example.umbrellaalert.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.umbrellaalert.data.preferences.PreferencesManager
import com.example.umbrellaalert.worker.WeatherNotificationWorker

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val preferencesManager = PreferencesManager(context)
            if (preferencesManager.dailyNotificationEnabled) {
                WeatherNotificationWorker.scheduleDailyCheck(
                    context = context,
                    targetHour = preferencesManager.notificationHour,
                    targetMinute = preferencesManager.notificationMinute
                )
            }
        }
    }
}
