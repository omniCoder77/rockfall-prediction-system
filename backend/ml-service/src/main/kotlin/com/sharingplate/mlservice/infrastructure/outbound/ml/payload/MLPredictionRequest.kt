package com.sharingplate.mlservice.infrastructure.outbound.ml.payload

import com.sharingplate.mlservice.infrastructure.inbound.kafka.dto.SensorFluctuationEvent

data class MLPredictionRequest(
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
) {
    constructor(fluctuationEvent: SensorFluctuationEvent) : this(
        timestamp = fluctuationEvent.timestamp,
        stationId = fluctuationEvent.stationId,
        lat = fluctuationEvent.lat,
        lon = fluctuationEvent.lon,
        elevationM = fluctuationEvent.elevationM,
        totalRainfallMm = fluctuationEvent.totalRainfallMm,
        dispIncMm_x = fluctuationEvent.dispIncMm_x,
        dispIncMm_y = fluctuationEvent.dispIncMm_y,
        strainIncMicro = fluctuationEvent.strainIncMicro,
        poreKpa = fluctuationEvent.poreKpa,
        vibration = fluctuationEvent.vibration,
        sensorType = fluctuationEvent.sensorType,
        field = fluctuationEvent.field,
        previousValue = fluctuationEvent.previousValue,
        currentValue = fluctuationEvent.currentValue
    )
}