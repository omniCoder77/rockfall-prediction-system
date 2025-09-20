package com.sharingplate.mlservice.infrastructure.outbound.mlapi

import java.util.UUID

data class PredictionResponse(
    val stationId: UUID,
    val timestamp: Long,
    val rockfallProbability: Float,
    val riskLevel: String,
    val vulnerableZones: List<VulnerableZone>? = null,
    val suggestedActions: List<String>? = null
)
