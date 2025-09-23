package com.sharingplate.sensorservice.domain.model

data class RainfallData(
    val timestamp: Long,
    val rainfallIncrement: Double,
    val totalRainfall: Double
)