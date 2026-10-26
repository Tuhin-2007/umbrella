package com.example.umbrellaalert.data.model

import com.google.gson.annotations.SerializedName

data class GeocodingResponse(
    @SerializedName("results") val results: List<GeocodingLocation>?
)

data class GeocodingLocation(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("country") val country: String?,
    @SerializedName("country_code") val countryCode: String?,
    @SerializedName("admin1") val adminArea: String?
) {
    val displayLocation: String
        get() {
            val parts = mutableListOf(name)
            if (!adminArea.isNullOrBlank() && adminArea != name) parts.add(adminArea)
            if (!country.isNullOrBlank()) parts.add(country)
            return parts.joinToString(", ")
        }
}
