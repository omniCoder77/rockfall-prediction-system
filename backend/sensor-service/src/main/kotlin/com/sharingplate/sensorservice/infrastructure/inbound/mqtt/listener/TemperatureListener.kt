package com.sharingplate.sensorservice.infrastructure.inbound.mqtt.listener

import com.google.gson.Gson
import com.sharingplate.sensorservice.domain.contants.SensorType
import com.sharingplate.sensorservice.domain.port.driven.TemperatureDataRepository
import com.sharingplate.sensorservice.infrastructure.inbound.mqtt.SensorService
import com.sharingplate.sensorservice.infrastructure.inbound.mqtt.dto.TemperatureDataEntity
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class TemperatureListener(
    @Lazy private val sensorService: SensorService,
    private val temperatureDataRepository: TemperatureDataRepository
) : MQTTEventListener {
    override fun onReceive(station: String, payload: String) {
        val data = Gson().fromJson(payload, TemperatureDataEntity::class.java)
        temperatureDataRepository.save(station, data.toDomain())
    }

    override fun onConnectionLost(station: String) {
        sensorService.removeSensor(station, SensorType.TEMPERATURE)
    }
}