package com.example.umbrellaalert.data.preferences

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var cityName: String
        get() = prefs.getString(KEY_CITY_NAME, "New York") ?: "New York"
        set(value) = prefs.edit().putString(KEY_CITY_NAME, value).apply()

    var latitude: Double
        get() = java.lang.Double.longBitsToDouble(
            prefs.getLong(KEY_LATITUDE, java.lang.Double.doubleToRawLongBits(40.7128))
        )
        set(value) = prefs.edit().putLong(
            KEY_LATITUDE,
            java.lang.Double.doubleToRawLongBits(value)
        ).apply()

    var longitude: Double
        get() = java.lang.Double.longBitsToDouble(
            prefs.getLong(KEY_LONGITUDE, java.lang.Double.doubleToRawLongBits(-74.0060))
        )
        set(value) = prefs.edit().putLong(
            KEY_LONGITUDE,
            java.lang.Double.doubleToRawLongBits(value)
        ).apply()

    var rainThreshold: Int
        get() = prefs.getInt(KEY_RAIN_THRESHOLD, 40)
        set(value) = prefs.edit().putInt(KEY_RAIN_THRESHOLD, value).apply()

    var dailyNotificationEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATION_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATION_ENABLED, value).apply()

    var notificationHour: Int
        get() = prefs.getInt(KEY_NOTIFICATION_HOUR, 7) // 7:00 AM default
        set(value) = prefs.edit().putInt(KEY_NOTIFICATION_HOUR, value).apply()

    var notificationMinute: Int
        get() = prefs.getInt(KEY_NOTIFICATION_MINUTE, 0)
        set(value) = prefs.edit().putInt(KEY_NOTIFICATION_MINUTE, value).apply()

    var lastNotifiedDate: String
        get() = prefs.getString(KEY_LAST_NOTIFIED_DATE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_NOTIFIED_DATE, value).apply()

    fun setLocation(name: String, lat: Double, lon: Double) {
        prefs.edit()
            .putString(KEY_CITY_NAME, name)
            .putLong(KEY_LATITUDE, java.lang.Double.doubleToRawLongBits(lat))
            .putLong(KEY_LONGITUDE, java.lang.Double.doubleToRawLongBits(lon))
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "umbrella_alert_prefs"
        private const val KEY_CITY_NAME = "city_name"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
        private const val KEY_RAIN_THRESHOLD = "rain_threshold"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        private const val KEY_NOTIFICATION_HOUR = "notification_hour"
        private const val KEY_NOTIFICATION_MINUTE = "notification_minute"
        private const val KEY_LAST_NOTIFIED_DATE = "last_notified_date"
    }
}
