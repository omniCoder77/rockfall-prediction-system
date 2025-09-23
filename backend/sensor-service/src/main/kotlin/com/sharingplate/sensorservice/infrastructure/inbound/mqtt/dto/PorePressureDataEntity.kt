package com.sharingplate.sensorservice.infrastructure.inbound.mqtt.dto

import com.sharingplate.sensorservice.domain.model.PorePressureData

data class PorePressureDataEntity(
    val timestamp: Long,
    val pressure: Double,
    val temperature: Double,
    val frequency: Double
) {
    fun toDomain() = PorePressureData(
        pressure = this.pressure,
        temperature = this.temperature,
        frequency = this.frequency
    )
}