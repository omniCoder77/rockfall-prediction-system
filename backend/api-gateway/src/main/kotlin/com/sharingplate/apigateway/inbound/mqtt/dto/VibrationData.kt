package com.sharingplate.apigateway.inbound.mqtt.dto

data class VibrationData(
    val timestamp: Long,
    val stationId: String,
    val accelX: Double,
    val accelY: Double,
    val accelZ: Double,
    val magnitude: Double
)