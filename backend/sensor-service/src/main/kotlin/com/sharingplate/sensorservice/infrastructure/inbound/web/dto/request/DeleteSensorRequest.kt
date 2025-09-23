package com.sharingplate.sensorservice.infrastructure.inbound.web.dto.request

import com.sharingplate.sensorservice.domain.contants.SensorType

data class DeleteSensorRequest(
    val stationId: String,
    val sensorType: SensorType
)
