package com.sharingplate.sensorservice.infrastructure.inbound.mqtt.listener

import com.google.gson.Gson
import com.sharingplate.sensorservice.domain.contants.SensorType
import com.sharingplate.sensorservice.domain.port.driven.VibrationDataRepository
import com.sharingplate.sensorservice.infrastructure.inbound.mqtt.SensorService
import com.sharingplate.sensorservice.infrastructure.inbound.mqtt.dto.VibrationDataEntity
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class VibrationListener(@Lazy private val sensorService: SensorService,
                        private val vibrationDataRepository: VibrationDataRepository
) : MQTTEventListener {
    override fun onReceive(station: String, payload: String) {
        val data = Gson().fromJson(payload, VibrationDataEntity::class.java)
        vibrationDataRepository.save(station, data.toDomain())
    }

    override fun onConnectionLost(station: String) {
        sensorService.removeSensor(station, SensorType.VIBRATION)
    }
}