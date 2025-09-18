package com.sharingplate.apigateway.inbound.mqtt.dto

data class TemperatureData(
    val timestamp: Long,
    val stationId: String,
    val temperature: Double,
    val humidity: Double
)