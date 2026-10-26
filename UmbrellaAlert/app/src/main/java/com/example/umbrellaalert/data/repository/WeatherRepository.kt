package com.example.umbrellaalert.data.repository

import com.example.umbrellaalert.data.api.RetrofitClient
import com.example.umbrellaalert.data.api.WeatherApiService
import com.example.umbrellaalert.data.model.GeocodingLocation
import com.example.umbrellaalert.data.model.HourlyRainItem
import com.example.umbrellaalert.data.model.UmbrellaDecision
import com.example.umbrellaalert.data.model.UrgencyLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherRepository(
    private val apiService: WeatherApiService = RetrofitClient.apiService
) {

    suspend fun getUmbrellaDecision(
        latitude: Double,
        longitude: Double,
        cityName: String,
        rainThreshold: Int
    ): Result<UmbrellaDecision> {
        return try {
            val response = apiService.getForecast(latitude = latitude, longitude = longitude)

            // Extract daily data
            val dailyMaxProb = response.daily?.precipitationProbabilityMax?.firstOrNull() ?: 0
            val dailyPrecipSum = response.daily?.precipitationSum?.firstOrNull() ?: 0.0
            val dailyWeatherCode = response.daily?.weatherCode?.firstOrNull() ?: 0
            val maxTemp = response.daily?.temperatureMax?.firstOrNull() ?: 20.0
            val minTemp = response.daily?.temperatureMin?.firstOrNull() ?: 10.0
            val currentTemp = response.current?.temperature ?: ((maxTemp + minTemp) / 2.0)

            // Extract hourly data (next 24 hours)
            val hourlyItems = mutableListOf<HourlyRainItem>()
            val hourlyTimes = response.hourly?.time ?: emptyList()
            val hourlyProbs = response.hourly?.precipitationProbability ?: emptyList()
            val hourlyPrecips = response.hourly?.precipitation ?: emptyList()
            val hourlyCodes = response.hourly?.weatherCode ?: emptyList()
            val hourlyTemps = response.hourly?.temperature ?: emptyList()

            var peakRainProb = dailyMaxProb
            var peakRainTime: String? = null

            val count = minOf(hourlyTimes.size, 24)
            for (i in 0 until count) {
                val timeStr = hourlyTimes.getOrNull(i) ?: continue
                val prob = hourlyProbs.getOrNull(i) ?: 0
                val precip = hourlyPrecips.getOrNull(i) ?: 0.0
                val code = hourlyCodes.getOrNull(i) ?: 0
                val temp = hourlyTemps.getOrNull(i) ?: currentTemp

                val formattedTime = formatTime(timeStr)
                hourlyItems.add(
                    HourlyRainItem(
                        timeLabel = formattedTime,
                        rainProbability = prob,
                        precipitationMm = precip,
                        temperature = temp,
                        weatherCode = code
                    )
                )

                if (prob > peakRainProb || (peakRainTime == null && prob >= rainThreshold)) {
                    peakRainProb = prob
                    peakRainTime = formattedTime
                }
            }

            val isRainWmoCode = isRainyWmoCode(dailyWeatherCode)
            val maxProb = maxOf(dailyMaxProb, peakRainProb)

            val urgency: UrgencyLevel
            val needsUmbrella: Boolean
            val headline: String
            val summary: String

            if (maxProb >= 60 || dailyPrecipSum >= 2.0 || dailyWeatherCode in listOf(63, 65, 81, 82, 95, 96, 99)) {
                needsUmbrella = true
                urgency = UrgencyLevel.HIGH
                headline = "☔ Take Your Umbrella Today!"
                summary = if (peakRainTime != null) {
                    "Heavy rain or showers expected around $peakRainTime with a $maxProb% chance ($dailyPrecipSum mm). Keep dry!"
                } else {
                    "High chance of rain today ($maxProb%, $dailyPrecipSum mm). Definitely take your umbrella before stepping out!"
                }
            } else if (maxProb >= rainThreshold || dailyPrecipSum >= 0.5 || isRainWmoCode) {
                needsUmbrella = true
                urgency = UrgencyLevel.MODERATE
                headline = "🌦️ Umbrella Recommended"
                summary = if (peakRainTime != null) {
                    "Rain showers possible around $peakRainTime ($maxProb% probability). Better carry an umbrella just in case!"
                } else {
                    "Moderate chance of rain ($maxProb%). Packing a compact umbrella is a good idea today."
                }
            } else {
                needsUmbrella = false
                urgency = UrgencyLevel.NONE
                headline = "☀️ No Umbrella Needed Today"
                summary = "Skies look clear! Rain chance is low at only $maxProb%. Enjoy your day without carrying extra gear."
            }

            val decision = UmbrellaDecision(
                needsUmbrella = needsUmbrella,
                urgencyLevel = urgency,
                headline = headline,
                summary = summary,
                maxRainProbability = maxProb,
                expectedRainMm = dailyPrecipSum,
                peakRainTime = peakRainTime,
                currentTemp = currentTemp,
                maxTemp = maxTemp,
                minTemp = minTemp,
                cityName = cityName,
                hourlyForecast = hourlyItems
            )
            Result.success(decision)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchCities(query: String): Result<List<GeocodingLocation>> {
        return try {
            val response = apiService.searchCity(cityName = query)
            Result.success(response.results ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isRainyWmoCode(code: Int): Boolean {
        return code in 51..67 || code in 80..82 || code in 95..99
    }

    private fun formatTime(isoTime: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
            val date = parser.parse(isoTime) ?: return isoTime
            val formatter = SimpleDateFormat("h a", Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            isoTime.takeLast(5)
        }
    }
}
