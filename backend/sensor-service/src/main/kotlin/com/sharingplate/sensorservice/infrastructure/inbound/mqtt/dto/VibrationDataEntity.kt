package com.sharingplate.sensorservice.infrastructure.inbound.mqtt.dto

import com.sharingplate.sensorservice.domain.model.VibrationData

data class VibrationDataEntity(
    val timestamp: Long,
    val accelX: Double,
    val accelY: Double,
    val accelZ: Double,
    val magnitude: Double
) {
    fun toDomain() = VibrationData(
        timestamp = this.timestamp,
        accelX = this.accelX,
        accelY = this.accelY,
        accelZ = this.accelZ,
        magnitude = this.magnitude
    )
}