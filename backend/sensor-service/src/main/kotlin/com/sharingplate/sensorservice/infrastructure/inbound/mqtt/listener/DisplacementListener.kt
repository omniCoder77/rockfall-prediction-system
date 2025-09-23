package com.sharingplate.sensorservice.infrastructure.inbound.mqtt.listener

import com.google.gson.Gson
import com.sharingplate.sensorservice.domain.contants.SensorType
import com.sharingplate.sensorservice.domain.port.driven.DisplacementDataRepository
import com.sharingplate.sensorservice.infrastructure.inbound.mqtt.SensorService
import com.sharingplate.sensorservice.infrastructure.inbound.mqtt.dto.DisplacementDataEntity
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class DisplacementListener(
    private val displacementDataRepository: DisplacementDataRepository,
    @Lazy private val sensorService: SensorService
) : MQTTEventListener {
    override fun onReceive(station: String, payload: String) {
        val data = Gson().fromJson(payload, DisplacementDataEntity::class.java)
        displacementDataRepository.save(data.toDomain(), station)
        val newValues = mapOf(
            "tiltX" to data.tiltX,
            "tiltY" to data.tiltY
        )
        sensorService.processSensorData(station, SensorType.DISPLACEMENT, newValues)
    }

    override fun onConnectionLost(station: String) {
        sensorService.markSensorAsDisconnected(station, SensorType.DISPLACEMENT)
    }
}