package com.sharingplate.mlservice.infrastructure.inbound.kafka.dto

data class SensorFluctuationEvent(
    val timestamp: Long,
    val stationId: String,
    val lat: Double,
    val lon: Double,
    val elevationM: Double,
    val totalRainfallMm: Double,
    val dispIncMm_x: Double,
    val dispIncMm_y: Double,
    val strainIncMicro: Double,
    val poreKpa: Double,
    val vibration: Double,
    val sensorType: String,
    val field: String,
    val previousValue: Double,
    val currentValue: Double
)