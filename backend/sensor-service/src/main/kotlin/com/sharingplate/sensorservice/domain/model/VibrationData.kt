package com.sharingplate.sensorservice.domain.model

data class VibrationData(
    val timestamp: Long,
    val accelX: Double,
    val accelY: Double,
    val accelZ: Double,
    val magnitude: Double
)