package com.sharingplate.sensorservice.infrastructure.inbound.mqtt.listener

import com.google.gson.Gson
import com.sharingplate.sensorservice.domain.contants.SensorType
import com.sharingplate.sensorservice.domain.port.driven.RainfallDataRepository
import com.sharingplate.sensorservice.infrastructure.inbound.mqtt.SensorService
import com.sharingplate.sensorservice.infrastructure.inbound.mqtt.dto.RainfallDataEntity
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class RainfallListener(
    @Lazy private val sensorService: SensorService, private val rainfallDataRepository: RainfallDataRepository
) : MQTTEventListener {
    override fun onReceive(station: String, payload: String) {
        val data = Gson().fromJson(payload, RainfallDataEntity::class.java)
        rainfallDataRepository.save(station, data.toDomain())
    }

    override fun onConnectionLost(station: String) {
        sensorService.removeSensor(station, SensorType.RAINFALL)
    }
}