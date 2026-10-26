package com.example.umbrellaalert.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.umbrellaalert.data.model.GeocodingLocation
import com.example.umbrellaalert.data.model.UmbrellaDecision
import com.example.umbrellaalert.data.preferences.PreferencesManager
import com.example.umbrellaalert.data.repository.WeatherRepository
import com.example.umbrellaalert.notification.NotificationHelper
import com.example.umbrellaalert.worker.WeatherNotificationWorker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class WeatherUiState(
    val isLoading: Boolean = false,
    val decision: UmbrellaDecision? = null,
    val errorMessage: String? = null,
    val currentCity: String = "New York",
    val isDailyNotificationEnabled: Boolean = true,
    val rainThreshold: Int = 40,
    val notificationHour: Int = 7,
    val notificationMinute: Int = 0,
    val isSearching: Boolean = false,
    val searchResults: List<GeocodingLocation> = emptyList(),
    val showCityDialog: Boolean = false,
    val showTimeDialog: Boolean = false
) {
    val formattedNotificationTime: String
        get() {
            val period = if (notificationHour < 12) "AM" else "PM"
            val displayHour = when {
                notificationHour == 0 -> 12
                notificationHour > 12 -> notificationHour - 12
                else -> notificationHour
            }
            return String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, notificationMinute, period)
        }
}

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = PreferencesManager(application)
    private val repository = WeatherRepository()

    private val _uiState = MutableStateFlow(
        WeatherUiState(
            currentCity = preferences.cityName,
            isDailyNotificationEnabled = preferences.dailyNotificationEnabled,
            rainThreshold = preferences.rainThreshold,
            notificationHour = preferences.notificationHour,
            notificationMinute = preferences.notificationMinute
        )
    )
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        refreshWeather()
        // Ensure daily check is scheduled if enabled
        if (preferences.dailyNotificationEnabled) {
            WeatherNotificationWorker.scheduleDailyCheck(
                context = getApplication(),
                targetHour = preferences.notificationHour,
                targetMinute = preferences.notificationMinute
            )
        }
    }

    fun refreshWeather() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.getUmbrellaDecision(
                latitude = preferences.latitude,
                longitude = preferences.longitude,
                cityName = preferences.cityName,
                rainThreshold = preferences.rainThreshold
            )
            result.onSuccess { decision ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        decision = decision,
                        currentCity = decision.cityName,
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Failed to fetch weather. Check internet connection."
                    )
                }
            }
        }
    }

    fun searchCities(query: String) {
        searchJob?.cancel()
        if (query.trim().length < 2) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(350) // debounce
            _uiState.update { it.copy(isSearching = true) }
            val result = repository.searchCities(query.trim())
            result.onSuccess { list ->
                _uiState.update { it.copy(searchResults = list, isSearching = false) }
            }.onFailure {
                _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            }
        }
    }

    fun selectCity(location: GeocodingLocation) {
        preferences.setLocation(location.name, location.latitude, location.longitude)
        _uiState.update {
            it.copy(
                currentCity = location.name,
                showCityDialog = false,
                searchResults = emptyList()
            )
        }
        refreshWeather()
    }

    fun toggleDailyNotification(enabled: Boolean) {
        preferences.dailyNotificationEnabled = enabled
        _uiState.update { it.copy(isDailyNotificationEnabled = enabled) }
        if (enabled) {
            WeatherNotificationWorker.scheduleDailyCheck(
                context = getApplication(),
                targetHour = preferences.notificationHour,
                targetMinute = preferences.notificationMinute
            )
        } else {
            WeatherNotificationWorker.cancelDailyCheck(getApplication())
        }
    }

    fun updateRainThreshold(threshold: Int) {
        preferences.rainThreshold = threshold
        _uiState.update { it.copy(rainThreshold = threshold) }
        refreshWeather()
    }

    fun updateNotificationTime(hour: Int, minute: Int) {
        preferences.notificationHour = hour
        preferences.notificationMinute = minute
        _uiState.update {
            it.copy(
                notificationHour = hour,
                notificationMinute = minute,
                showTimeDialog = false
            )
        }
        if (preferences.dailyNotificationEnabled) {
            WeatherNotificationWorker.scheduleDailyCheck(
                context = getApplication(),
                targetHour = hour,
                targetMinute = minute
            )
        }
    }

    fun sendTestNotification() {
        val decision = _uiState.value.decision
        val headline = decision?.headline ?: "☔ Umbrella Alert Test"
        val message = decision?.summary ?: "Test notification: Keep an umbrella handy today! [${_uiState.value.currentCity}]"

        NotificationHelper.showNotification(
            context = getApplication(),
            title = headline,
            message = message,
            isRainExpected = decision?.needsUmbrella ?: true
        )
    }

    fun setShowCityDialog(show: Boolean) {
        _uiState.update { it.copy(showCityDialog = show, searchResults = emptyList()) }
    }

    fun setShowTimeDialog(show: Boolean) {
        _uiState.update { it.copy(showTimeDialog = show) }
    }
}
