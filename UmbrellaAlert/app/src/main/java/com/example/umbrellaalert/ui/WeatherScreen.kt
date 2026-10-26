package com.example.umbrellaalert.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.umbrellaalert.data.model.HourlyRainItem
import com.example.umbrellaalert.data.model.UrgencyLevel
import com.example.umbrellaalert.ui.theme.BluePrimary
import com.example.umbrellaalert.ui.theme.RainAlertHigh
import com.example.umbrellaalert.ui.theme.RainAlertModerate
import com.example.umbrellaalert.ui.theme.RainAlertNone
import com.example.umbrellaalert.ui.theme.RainBlue
import com.example.umbrellaalert.ui.theme.SunYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.setShowCityDialog(true) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = uiState.currentCity,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshWeather() },
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BluePrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error banner if any
            if (uiState.errorMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(14.dp),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Main Umbrella Status Card
            item {
                UmbrellaStatusHeroCard(uiState = uiState)
            }

            // Hourly Rain Forecast Section
            item {
                HourlyForecastCard(uiState = uiState)
            }

            // Notification Settings Card
            item {
                NotificationSettingsCard(
                    uiState = uiState,
                    onToggleNotification = { viewModel.toggleDailyNotification(it) },
                    onThresholdChange = { viewModel.updateRainThreshold(it) },
                    onPickTimeClick = {
                        val timePicker = TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                viewModel.updateNotificationTime(hour, minute)
                            },
                            uiState.notificationHour,
                            uiState.notificationMinute,
                            false
                        )
                        timePicker.show()
                    },
                    onTestNotificationClick = { viewModel.sendTestNotification() }
                )
            }
        }
    }

    // City Search Dialog
    if (uiState.showCityDialog) {
        CitySearchDialog(
            uiState = uiState,
            onDismiss = { viewModel.setShowCityDialog(false) },
            onSearchQueryChanged = { viewModel.searchCities(it) },
            onCitySelected = { viewModel.selectCity(it) }
        )
    }
}

@Composable
fun UmbrellaStatusHeroCard(uiState: WeatherUiState) {
    val decision = uiState.decision

    val gradientColors = when (decision?.urgencyLevel) {
        UrgencyLevel.HIGH -> listOf(Color(0xFFE53935), Color(0xFFC2185B))
        UrgencyLevel.MODERATE -> listOf(Color(0xFFFB8C00), Color(0xFFE65100))
        UrgencyLevel.NONE -> listOf(Color(0xFF43A047), Color(0xFF1B5E20))
        null -> listOf(Color(0xFF1E88E5), Color(0xFF1565C0))
    }

    val iconVector = when (decision?.urgencyLevel) {
        UrgencyLevel.HIGH -> Icons.Outlined.Umbrella
        UrgencyLevel.MODERATE -> Icons.Default.WaterDrop
        UrgencyLevel.NONE -> Icons.Default.WbSunny
        null -> Icons.Outlined.Umbrella
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(gradientColors))
                .padding(22.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Large circular icon container
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White.copy(alpha = 0.22f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = "Weather Status Icon",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Headline
                Text(
                    text = decision?.headline ?: "Checking Weather...",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Summary text
                Text(
                    text = decision?.summary ?: "Fetching forecast from internet to determine umbrella necessity...",
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Stat metrics row
                if (decision != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(
                            label = "Rain Chance",
                            value = "${decision.maxRainProbability}%"
                        )
                        StatItem(
                            label = "Rain Volume",
                            value = "${decision.expectedRainMm} mm"
                        )
                        StatItem(
                            label = "Temp Range",
                            value = "${decision.minTemp.toInt()}° - ${decision.maxTemp.toInt()}°C"
                        )
                        StatItem(
                            label = "Peak Rain",
                            value = decision.peakRainTime ?: "None"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun HourlyForecastCard(uiState: WeatherUiState) {
    val hourlyList = uiState.decision?.hourlyForecast ?: emptyList()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = RainBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Today's Rain Chance by Hour",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (hourlyList.isEmpty()) {
                Text(
                    text = "No hourly forecast available yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(hourlyList) { item ->
                        HourlyForecastItem(item = item)
                    }
                }
            }
        }
    }
}

@Composable
fun HourlyForecastItem(item: HourlyRainItem) {
    val isRaining = item.rainProbability >= 40
    val badgeColor = when {
        item.rainProbability >= 60 -> RainAlertHigh
        item.rainProbability >= 30 -> RainAlertModerate
        else -> RainAlertNone
    }

    Card(
        modifier = Modifier.width(68.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRaining) RainBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
        ) {
            Text(
                text = item.timeLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Icon(
                imageVector = if (item.rainProbability >= 40) Icons.Default.WaterDrop else Icons.Default.WbSunny,
                contentDescription = null,
                tint = if (item.rainProbability >= 40) RainBlue else SunYellow,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${item.rainProbability}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = badgeColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${item.temperature.toInt()}°C",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun NotificationSettingsCard(
    uiState: WeatherUiState,
    onToggleNotification: (Boolean) -> Unit,
    onThresholdChange: (Int) -> Unit,
    onPickTimeClick: () -> Unit,
    onTestNotificationClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = BluePrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Umbrella Alert Notifications",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Switch for Daily Reminder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Daily Morning Reminder",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Auto-checks internet weather every morning and sends notification if you need an umbrella.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = uiState.isDailyNotificationEnabled,
                    onCheckedChange = onToggleNotification
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Schedule Time Picker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = uiState.isDailyNotificationEnabled) { onPickTimeClick() }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reminder Time",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = uiState.formattedNotificationTime,
                    fontWeight = FontWeight.Bold,
                    color = if (uiState.isDailyNotificationEnabled) BluePrimary else Color.Gray,
                    fontSize = 14.sp
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Sensitivity threshold
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Rain Alert Sensitivity",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Alert if Rain ≥ ${uiState.rainThreshold}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
                }
                Slider(
                    value = uiState.rainThreshold.toFloat(),
                    onValueChange = { onThresholdChange(it.toInt()) },
                    valueRange = 10f..80f,
                    steps = 6,
                    enabled = uiState.isDailyNotificationEnabled
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Test Notification Button
            Button(
                onClick = onTestNotificationClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Send Test Notification Now")
            }
        }
    }
}

@Composable
fun CitySearchDialog(
    uiState: WeatherUiState,
    onDismiss: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onCitySelected: (com.example.umbrellaalert.data.model.GeocodingLocation) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Select City", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onSearchQueryChanged(it)
                    },
                    label = { Text("Search city (e.g. London, Tokyo)") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                onSearchQueryChanged("")
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.isSearching) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    }
                } else if (uiState.searchResults.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        items(uiState.searchResults) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCitySelected(item) }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = BluePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = item.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    val subtitle = listOfNotNull(item.adminArea, item.country).joinToString(", ")
                                    if (subtitle.isNotEmpty()) {
                                        Text(
                                            text = subtitle,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                } else if (searchQuery.length >= 2) {
                    Text(
                        text = "No matching cities found.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
