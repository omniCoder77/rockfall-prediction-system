package com.sharingplate.sensorservice.infrastructure.inbound.mqtt.dto

import com.sharingplate.sensorservice.domain.model.TemperatureData

data class TemperatureDataEntity(
    val timestamp: Long,
    val temperature: Double,
    val humidity: Double
) {
    fun toDomain() = TemperatureData(
        timestamp = this.timestamp,
        temperature = this.temperature,
        humidity = this.humidity
    )
}