# ☔ Umbrella Alert - Smart Weather & Rain Notification Android App

An Android application built with **Kotlin**, **Jetpack Compose (Material 3)**, and **Android WorkManager** that fetches real-time weather forecasts from the internet and sends reminders whenever you need to carry an umbrella.

---

## 🌟 Key Features

1. **🌐 Real-Time Internet Weather Data**
   - Integrates with the **Open-Meteo Forecast & Geocoding APIs**.
   - **No API keys or credit cards required**: Works out of the box with zero setup.
   - Fetches current weather, hourly precipitation chances, daily maximum rainfall probabilities, and total precipitation volume (mm).

2. **🧠 Smart Umbrella Decision Engine**
   - Automatically assesses whether an umbrella is needed:
     - ☔ **High Urgency**: High probability of rain (≥ 60%), heavy rainfall volume (≥ 2.0 mm), or severe weather (thunderstorms, heavy showers).
     - 🌦️ **Moderate Urgency**: Moderate probability (≥ configurable threshold, default 40%) or light showers/drizzle.
     - ☀️ **No Umbrella Needed**: Clear skies or low probability (< threshold).
   - Identifies peak rain hours (e.g., *"Rain expected around 2:00 PM with 85% chance"*).

3. **⏰ Background Notification Reminder (WorkManager)**
   - Schedules a daily check (default: **7:00 AM**) using Android's **`WorkManager`** (`PeriodicWorkRequest`).
   - Requires internet network connectivity constraint (`NetworkType.CONNECTED`).
   - Posts a high-priority notification with sound and vibration if rain is forecast for the day.
   - Automatically re-registers the schedule upon device reboot via `BootReceiver`.

4. **🔔 Instant Notification Testing**
   - Includes a **"Send Test Notification Now"** button in the app settings, allowing you to test the notification appearance immediately without waiting for the scheduled time.

5. **🌍 Worldwide City Search & Geocoding**
   - Change your location anytime with the interactive city search dialog.
   - Search any city, state, or country worldwide to fetch local forecasts instantly.

6. **🎨 Modern Jetpack Compose UI**
   - Dynamic Material 3 design that changes card colors and icons based on weather urgency.
   - Horizontal hourly breakdown showing rain probability percentages and temperatures.
   - Rain sensitivity slider to customize when you want to be alerted (from 10% to 80%).
   - Android 13+ (API 33+) runtime notification permission request.

---

## 📁 Project Architecture

```
UmbrellaAlert/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml              # Permissions & app configuration
│   │   ├── java/com/example/umbrellaalert/
│   │   │   ├── UmbrellaApp.kt              # Application class & NotificationChannel setup
│   │   │   ├── MainActivity.kt             # Jetpack Compose host & permission requester
│   │   │   ├── data/
│   │   │   │   ├── api/
│   │   │   │   │   ├── WeatherApiService.kt # Retrofit interface for Open-Meteo
│   │   │   │   │   └── RetrofitClient.kt   # OkHttp & Retrofit client singleton
│   │   │   │   ├── model/
│   │   │   │   │   ├── WeatherResponse.kt  # Weather data models & UmbrellaDecision
│   │   │   │   │   └── GeocodingResponse.kt# City search models
│   │   │   │   ├── preferences/
│   │   │   │   │   └── PreferencesManager.kt # SharedPreferences storage
│   │   │   │   └── repository/
│   │   │   │       └── WeatherRepository.kt# Weather fetching & umbrella logic
│   │   │   ├── notification/
│   │   │   │   └── NotificationHelper.kt   # System notification builder & channel
│   │   │   ├── receiver/
│   │   │   │   └── BootReceiver.kt         # Reschedules WorkManager on device boot
│   │   │   ├── ui/
│   │   │   │   ├── theme/
│   │   │   │   │   ├── Color.kt            # Color definitions
│   │   │   │   │   ├── Theme.kt            # Material 3 theme & dynamic color
│   │   │   │   │   └── Type.kt             # Typography
│   │   │   │   ├── WeatherScreen.kt        # Jetpack Compose UI components
│   │   │   │   └── WeatherViewModel.kt     # State management & coroutine execution
│   │   │   └── worker/
│   │   │       └── WeatherNotificationWorker.kt # WorkManager background task
│   │   └── res/
│   │       ├── drawable/                   # Weather & umbrella vector icons
│   │       ├── values/                     # strings.xml, colors.xml, themes.xml
│   │       └── xml/                        # Backup & data extraction rules
│   └── build.gradle.kts                    # App-level dependencies & build rules
├── build.gradle.kts                        # Root build configuration
├── settings.gradle.kts                     # Project & repository settings
└── gradle.properties                       # Memory & AndroidX options
```

---

## 🛠️ How to Open and Run in Android Studio

1. **Open Android Studio**:
   - Select **File** > **Open...**
   - Browse to the folder: `c:\Users\tuhin\Downloads\app projects\UmbrellaAlert`
   - Click **OK**.

2. **Gradle Sync**:
   - Android Studio will automatically recognize the project and download the Gradle wrapper and dependencies.
   - Ensure you are using **JDK 17** (configured automatically in Android Studio Hedgehog / Iguana / Jellyfish / Ladybug under *Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK*).

3. **Run on Device or Emulator**:
   - Connect an Android device with USB debugging enabled, or start an Android Virtual Device (AVD).
   - Click the green **Run** (▶) button in Android Studio.
   - When prompted, grant the **Notification Permission** so the app can send you umbrella alerts.

4. **Verify Functionality**:
   - The app fetches your forecast immediately and presents the umbrella recommendation.
   - Tap **"Send Test Notification Now"** to verify that notifications appear in the status bar with title, message, and icon.
   - Tap on the city name in the top bar to search for another city (e.g., *London, Tokyo, Seattle*).
   - Adjust the notification time or rain sensitivity slider to your preference.
