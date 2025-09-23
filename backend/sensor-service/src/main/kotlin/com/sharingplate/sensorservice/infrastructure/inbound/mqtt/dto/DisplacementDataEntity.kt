package com.sharingplate.sensorservice.infrastructure.inbound.mqtt.dto

data class DisplacementDataEntity(
    val tiltX: Double,
    val tiltY: Double,
    val temperature: Double,
) {
    fun toDomain() = com.sharingplate.sensorservice.domain.model.DisplacementData(
        tiltX = tiltX,
        tiltY = tiltY,
        temperature = temperature,
    )
}