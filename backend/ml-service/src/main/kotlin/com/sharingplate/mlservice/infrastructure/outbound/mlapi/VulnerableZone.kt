package com.sharingplate.mlservice.infrastructure.outbound.mlapi

data class VulnerableZone(
    val id: String,
    val coordinates: List<Float>,
    val probability: Float
)
