package com.example.umbrellaalert.data.model

import com.google.gson.annotations.SerializedName

data class WeatherApiResponse(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("timezone") val timezone: String?,
    @SerializedName("current") val current: CurrentWeather?,
    @SerializedName("hourly") val hourly: HourlyWeather?,
    @SerializedName("daily") val daily: DailyWeather?
)

data class CurrentWeather(
    @SerializedName("time") val time: String?,
    @SerializedName("temperature_2m") val temperature: Double?,
    @SerializedName("relative_humidity_2m") val humidity: Int?,
    @SerializedName("precipitation") val precipitation: Double?,
    @SerializedName("weather_code") val weatherCode: Int?
)

data class HourlyWeather(
    @SerializedName("time") val time: List<String>?,
    @SerializedName("temperature_2m") val temperature: List<Double?>?,
    @SerializedName("precipitation_probability") val precipitationProbability: List<Int?>?,
    @SerializedName("precipitation") val precipitation: List<Double?>?,
    @SerializedName("weather_code") val weatherCode: List<Int?>?
)

data class DailyWeather(
    @SerializedName("time") val time: List<String>?,
    @SerializedName("weather_code") val weatherCode: List<Int?>?,
    @SerializedName("temperature_2m_max") val temperatureMax: List<Double?>?,
    @SerializedName("temperature_2m_min") val temperatureMin: List<Double?>?,
    @SerializedName("precipitation_probability_max") val precipitationProbabilityMax: List<Int?>?,
    @SerializedName("precipitation_sum") val precipitationSum: List<Double?>?
)

enum class UrgencyLevel {
    HIGH,
    MODERATE,
    NONE
}

data class HourlyRainItem(
    val timeLabel: String,
    val rainProbability: Int,
    val precipitationMm: Double,
    val temperature: Double,
    val weatherCode: Int
)

data class UmbrellaDecision(
    val needsUmbrella: Boolean,
    val urgencyLevel: UrgencyLevel,
    val headline: String,
    val summary: String,
    val maxRainProbability: Int,
    val expectedRainMm: Double,
    val peakRainTime: String?,
    val currentTemp: Double,
    val maxTemp: Double,
    val minTemp: Double,
    val cityName: String,
    val hourlyForecast: List<HourlyRainItem>
)
