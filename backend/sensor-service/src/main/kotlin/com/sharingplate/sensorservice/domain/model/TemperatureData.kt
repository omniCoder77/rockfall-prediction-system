package com.sharingplate.sensorservice.domain.model

data class TemperatureData(
    val timestamp: Long,
    val temperature: Double,
    val humidity: Double
)