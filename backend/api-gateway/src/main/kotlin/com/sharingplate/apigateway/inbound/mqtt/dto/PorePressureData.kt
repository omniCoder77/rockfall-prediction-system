package com.sharingplate.apigateway.inbound.mqtt.dto

data class PorePressureData(
    val timestamp: Long,
    val stationId: String,
    val pressure: Double,
    val temperature: Double,
    val frequency: Double
)