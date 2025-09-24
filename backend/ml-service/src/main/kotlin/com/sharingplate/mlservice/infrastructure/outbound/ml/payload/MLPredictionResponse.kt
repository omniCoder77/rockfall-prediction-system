package com.sharingplate.mlservice.infrastructure.outbound.ml.payload

data class MLPredictionResponse(
    val riskLevel: Double,
    val stationId: String,
    val timestamp: Long,
    val title: String,
    val description: String
)