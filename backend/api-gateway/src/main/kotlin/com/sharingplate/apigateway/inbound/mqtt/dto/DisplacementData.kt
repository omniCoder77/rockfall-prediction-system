package com.sharingplate.apigateway.inbound.mqtt.dto

data class DisplacementData(
    val timestamp: Long,
    val stationId: String,
    val tiltX: Double,
    val tiltY: Double,
    val temperature: Double,
    val batteryLevel: Int
)