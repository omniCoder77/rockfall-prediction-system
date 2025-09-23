package com.sharingplate.sensorservice.domain.model

data class StrainData(
    val timestamp: Long,
    val strainValue: Double,
    val temperature: Double,
    val frequency: Double
)