package com.sharingplate.sensorservice.infrastructure.inbound.mqtt.dto

data class SensorStatus(
    val stationId: String, val sensorType: String, val status: String,
    val timestamp: Long
)