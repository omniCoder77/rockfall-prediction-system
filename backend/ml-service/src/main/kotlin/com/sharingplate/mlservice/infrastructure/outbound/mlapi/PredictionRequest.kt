package com.sharingplate.mlservice.infrastructure.outbound.mlapi

import java.util.UUID

data class PredictionRequest(
    val stationId: UUID,
    val demData: List<List<Float>>,
    val droneImageryFeatures: List<Float>,
    val geotechnicalSensorData: Map<String, List<Float>>,
    val environmentalFactors: Map<String, Float>
)