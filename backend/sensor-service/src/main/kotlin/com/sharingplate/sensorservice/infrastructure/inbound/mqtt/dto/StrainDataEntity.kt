package com.sharingplate.sensorservice.infrastructure.inbound.mqtt.dto

import com.sharingplate.sensorservice.domain.model.StrainData

data class StrainDataEntity(
    val timestamp: Long,
    val strainValue: Double,
    val temperature: Double,
    val frequency: Double
) {
    fun toDomain() = StrainData(
        timestamp = this.timestamp,
        strainValue = this.strainValue,
        temperature = this.temperature,
        frequency = this.frequency
    )
}