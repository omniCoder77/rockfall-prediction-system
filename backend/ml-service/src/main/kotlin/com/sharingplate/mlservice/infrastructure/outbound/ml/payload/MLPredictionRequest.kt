package com.sharingplate.mlservice.infrastructure.outbound.ml.payload

data class MLPredictionRequest(
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
    val blastFlag: Int,
    val sensorStatus: String,
    val cumulativeDispMm: Double,
    val dispRate6h: Double,
    val strain6h: Double,
    val pore6h: Double,
    val vib6h: Double,
    val riskScoreRaw: Double,
    val riskScoreNorm: Double,
    val riskLabel: String,
    val eventProb: Double,
    val rockfallEvent: Int
)