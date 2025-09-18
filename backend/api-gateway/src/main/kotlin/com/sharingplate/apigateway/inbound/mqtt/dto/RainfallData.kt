package com.sharingplate.apigateway.inbound.mqtt.dto

data class RainfallData(
    val timestamp: Long,
    val stationId: String,
    val rainfallIncrement: Double,
    val totalRainfall: Double
)