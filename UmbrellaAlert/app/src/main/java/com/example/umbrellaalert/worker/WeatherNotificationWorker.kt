package com.example.umbrellaalert.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.umbrellaalert.data.preferences.PreferencesManager
import com.example.umbrellaalert.data.repository.WeatherRepository
import com.example.umbrellaalert.notification.NotificationHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class WeatherNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val preferencesManager = PreferencesManager(context)
    private val weatherRepository = WeatherRepository()

    override suspend fun doWork(): Result {
        // If daily notification is turned off by user, do nothing
        if (!preferencesManager.dailyNotificationEnabled) {
            return Result.success()
        }

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Avoid sending duplicate notifications on the same day unless it's a test run
        val isForceRun = inputData.getBoolean(KEY_FORCE_RUN, false)
        if (!isForceRun && preferencesManager.lastNotifiedDate == todayDate) {
            return Result.success()
        }

        val lat = preferencesManager.latitude
        val lon = preferencesManager.longitude
        val cityName = preferencesManager.cityName
        val threshold = preferencesManager.rainThreshold

        val result = weatherRepository.getUmbrellaDecision(
            latitude = lat,
            longitude = lon,
            cityName = cityName,
            rainThreshold = threshold
        )

        return if (result.isSuccess) {
            val decision = result.getOrNull()
            if (decision != null) {
                // If umbrella is needed or if force-run is requested
                if (decision.needsUmbrella || isForceRun) {
                    val notifText = buildString {
                        append(decision.summary)
                        append(" [${decision.cityName}]")
                    }
                    NotificationHelper.showNotification(
                        context = context,
                        title = decision.headline,
                        message = notifText,
                        isRainExpected = decision.needsUmbrella
                    )
                    preferencesManager.lastNotifiedDate = todayDate
                }
            }
            Result.success()
        } else {
            // If network failure, retry later
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME_PERIODIC = "DailyUmbrellaWeatherCheck"
        const val WORK_NAME_ONE_TIME = "InstantUmbrellaWeatherCheck"
        const val KEY_FORCE_RUN = "key_force_run"

        fun scheduleDailyCheck(context: Context, targetHour: Int = 7, targetMinute: Int = 0) {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, targetHour)
                set(Calendar.MINUTE, targetMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(now)) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val initialDelayMillis = target.timeInMillis - now.timeInMillis

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<WeatherNotificationWorker>(
                24, TimeUnit.HOURS,
                15, TimeUnit.MINUTES // flex interval
            )
                .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicRequest
            )
        }

        fun cancelDailyCheck(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_PERIODIC)
        }

        fun runImmediateCheck(context: Context, forceNotification: Boolean = false) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val oneTimeRequest = OneTimeWorkRequestBuilder<WeatherNotificationWorker>()
                .setConstraints(constraints)
                .setInputData(
                    androidx.work.Data.Builder()
                        .putBoolean(KEY_FORCE_RUN, forceNotification)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME_ONE_TIME,
                ExistingWorkPolicy.REPLACE,
                oneTimeRequest
            )
        }
    }
}
