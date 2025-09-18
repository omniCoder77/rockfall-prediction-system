package com.sharingplate.apigateway.inbound.mqtt.dto

data class StrainData(
    val timestamp: Long,
    val stationId: String,
    val strainValue: Double,
    val temperature: Double,
    val frequency: Double
)