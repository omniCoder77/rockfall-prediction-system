package com.sharingplate.mlservice.infrastructure.outbound.ml.payload

data class MLPredictionResponse(
    val risk_level: Double,
    val station_id: String,
    val timestamp: Double
)