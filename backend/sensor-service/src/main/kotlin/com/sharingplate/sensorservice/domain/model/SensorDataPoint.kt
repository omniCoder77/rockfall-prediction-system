package com.sharingplate.sensorservice.domain.model

import java.time.Instant

data class SensorDataPoint(
    val time: Instant,
    val value: Double,
    val measurement: String,
    val field: String,
    val station: String
)