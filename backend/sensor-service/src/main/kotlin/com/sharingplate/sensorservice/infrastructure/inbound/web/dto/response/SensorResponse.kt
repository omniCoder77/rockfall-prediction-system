package com.sharingplate.sensorservice.infrastructure.inbound.web.dto.response

import com.sharingplate.sensorservice.domain.contants.SensorType

data class SensorResponse(
    val stationId: String,
    val sensor: SensorType
)
