package com.example.data.weather

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

sealed interface WeatherTelemetryState {
    data object Idle : WeatherTelemetryState
    data class Loading(val sourceLabel: String) : WeatherTelemetryState
    data class Ready(
        val temperatureC: Int,
        val conditionLabel: String,
        val weatherCode: Int,
        val cityLabel: String,
        val isDay: Boolean,
        val fetchedAtMs: Long
    ) : WeatherTelemetryState {
        val compactReadout: String
            get() = if (cityLabel.isNotBlank()) {
                "${temperatureC}° ${conditionLabel.uppercase()} • ${cityLabel.uppercase()}"
            } else {
                "${temperatureC}° ${conditionLabel.uppercase()}"
            }
    }
    data class PermissionDenied(val reason: String) : WeatherTelemetryState
    data class Unavailable(val message: String) : WeatherTelemetryState
}

/**
 * Live weather telemetry client powered by Open-Meteo (zero API key required),
 * supporting manual city search and optional coarse device location (`ACCESS_COARSE_LOCATION`),
 * plus clean battery percentage extraction for lock-screen Studio widgets.
 */
object OpenMeteoWeatherRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val _state = MutableStateFlow<WeatherTelemetryState>(WeatherTelemetryState.Idle)
    val state: StateFlow<WeatherTelemetryState> = _state.asStateFlow()

    suspend fun refreshByCity(city: String): WeatherTelemetryState {
        val clean = city.trim()
        if (clean.isEmpty()) {
            val next = WeatherTelemetryState.Idle
            _state.value = next
            return next
        }
        _state.value = WeatherTelemetryState.Loading(clean)
        val result = withContext(Dispatchers.IO) {
            runCatching {
                val encoded = URLEncoder.encode(clean, "UTF-8")
                val geoReq = Request.Builder()
                    .url("https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=1&language=en&format=json")
                    .build()
                val geoBody = client.newCall(geoReq).execute().use { resp ->
                    if (!resp.isSuccessful) error("Geocoding HTTP ${resp.code}")
                    resp.body?.string().orEmpty()
                }
                val results = JSONObject(geoBody).optJSONArray("results")
                if (results == null || results.length() == 0) {
                    return@runCatching WeatherTelemetryState.Unavailable("City \"$clean\" not found")
                }
                val first = results.getJSONObject(0)
                val lat = first.getDouble("latitude")
                val lon = first.getDouble("longitude")
                val resolvedCity = first.optString("name", clean).ifBlank { clean }
                fetchForecastForCoordinates(lat, lon, resolvedCity)
            }.getOrElse { err ->
                WeatherTelemetryState.Unavailable(err.localizedMessage ?: "Weather offline")
            }
        }
        _state.value = result
        return result
    }

    suspend fun refreshByDeviceCoarseLocation(context: Context): WeatherTelemetryState {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            val denied = WeatherTelemetryState.PermissionDenied("Location permission not granted")
            _state.value = denied
            return denied
        }

        _state.value = WeatherTelemetryState.Loading("Current Area")
        val loc = readCoarseLocation(context)
        if (loc == null) {
            val unavailable = WeatherTelemetryState.Unavailable("Coarse location unavailable")
            _state.value = unavailable
            return unavailable
        }
        val result = withContext(Dispatchers.IO) {
            runCatching {
                fetchForecastForCoordinates(loc.latitude, loc.longitude, "Local")
            }.getOrElse { err ->
                WeatherTelemetryState.Unavailable(err.localizedMessage ?: "Weather offline")
            }
        }
        _state.value = result
        return result
    }

    fun setPermissionDeniedState(message: String = "Location permission declined") {
        _state.value = WeatherTelemetryState.PermissionDenied(message)
    }

    fun resetToIdle() {
        _state.value = WeatherTelemetryState.Idle
    }

    private fun fetchForecastForCoordinates(
        lat: Double,
        lon: Double,
        cityLabel: String
    ): WeatherTelemetryState {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code,is_day&timezone=auto"
        val req = Request.Builder().url(url).build()
        val body = client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("Weather HTTP ${resp.code}")
            resp.body?.string().orEmpty()
        }
        return parseForecastJson(body, cityLabel)
    }

    internal fun parseForecastJson(json: String, cityLabel: String): WeatherTelemetryState {
        val root = JSONObject(json)
        val current = root.optJSONObject("current")
            ?: return WeatherTelemetryState.Unavailable("Malformed weather payload")
        val tempC = current.optDouble("temperature_2m", 20.0).roundToInt()
        val code = current.optInt("weather_code", 0)
        val isDay = current.optInt("is_day", 1) == 1
        return WeatherTelemetryState.Ready(
            temperatureC = tempC,
            conditionLabel = mapWmoCodeToLabel(code, isDay),
            weatherCode = code,
            cityLabel = cityLabel,
            isDay = isDay,
            fetchedAtMs = System.currentTimeMillis()
        )
    }

    fun mapWmoCodeToLabel(code: Int, isDay: Boolean = true): String = when (code) {
        0 -> if (isDay) "Clear" else "Clear Night"
        1, 2 -> "Partly Cloudy"
        3 -> "Overcast"
        45, 48 -> "Fog"
        51, 53, 55, 56, 57 -> "Drizzle"
        61, 63, 65, 66, 67 -> "Rain"
        71, 73, 75, 77 -> "Snow"
        80, 81, 82 -> "Showers"
        85, 86 -> "Snow Showers"
        95, 96, 99 -> "Storm"
        else -> "Mild"
    }

    @Suppress("MissingPermission")
    private fun readCoarseLocation(context: Context): Location? {
        return runCatching {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
            val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            providers.firstNotNullOfOrNull { provider ->
                if (runCatching { lm.isProviderEnabled(provider) }.getOrDefault(false)) {
                    lm.getLastKnownLocation(provider)
                } else {
                    null
                }
            }
        }.getOrNull()
    }

    fun readBatteryPercent(context: Context): Int {
        return runCatching {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val direct = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
            if (direct in 1..100) return direct
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
            if (level >= 0 && scale > 0) ((level * 100f) / scale).roundToInt().coerceIn(1, 100) else 86
        }.getOrDefault(86)
    }
}
