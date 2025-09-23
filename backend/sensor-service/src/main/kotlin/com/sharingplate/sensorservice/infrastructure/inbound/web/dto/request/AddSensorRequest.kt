package com.sharingplate.sensorservice.infrastructure.inbound.web.dto.request

import com.sharingplate.sensorservice.domain.contants.SensorType

data class AddSensorRequest(
    val stationId: String,
    val sensorType: SensorType
)