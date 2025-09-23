package com.sharingplate.sensorservice.infrastructure.inbound.mqtt.listener

import com.google.gson.Gson
import com.sharingplate.sensorservice.domain.contants.SensorType
import com.sharingplate.sensorservice.domain.port.driven.PorePressureDataRepository
import com.sharingplate.sensorservice.infrastructure.inbound.mqtt.SensorService
import com.sharingplate.sensorservice.infrastructure.inbound.mqtt.dto.PorePressureDataEntity
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class PorePressureListener(
    @Lazy private val sensorService: SensorService,
    private val porePressureDataRepository: PorePressureDataRepository
) : MQTTEventListener {
    override fun onReceive(station: String, payload: String) {
        val data = Gson().fromJson(payload, PorePressureDataEntity::class.java)
        porePressureDataRepository.save(station, data.toDomain())
    }

    override fun onConnectionLost(station: String) {
        sensorService.removeSensor(station, SensorType.PORE_PRESSURE)
    }
}