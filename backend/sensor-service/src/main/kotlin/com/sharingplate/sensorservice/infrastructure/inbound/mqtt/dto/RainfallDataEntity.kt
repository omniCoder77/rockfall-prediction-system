package com.sharingplate.sensorservice.infrastructure.inbound.mqtt.dto

import com.sharingplate.sensorservice.domain.model.RainfallData

data class RainfallDataEntity(
    val timestamp: Long,
    val rainfallIncrement: Double,
    val totalRainfall: Double
) {
    fun toDomain() = RainfallData(
        timestamp = this.timestamp,
        rainfallIncrement = this.rainfallIncrement,
        totalRainfall = this.totalRainfall
    )
}