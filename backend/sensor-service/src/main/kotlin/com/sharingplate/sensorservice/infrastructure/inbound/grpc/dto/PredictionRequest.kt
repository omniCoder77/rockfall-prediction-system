package com.sharingplate.sensorservice.infrastructure.inbound.grpc.dto

data class PredictionRequest(
    val timestamp: Double,
    val stationId: String,
    val zone: String,
    val lat: Double,
    val lon: Double,
    val elevationM: Double,
    val rainfallMm: Double,
    val dispIncMm: Double,
    val strainIncMicro: Double,
    val poreKpa: Double,
    val vibration: Double,
) {
    companion object {
    }
}