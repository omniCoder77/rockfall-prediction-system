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
        val newValues = mapOf(
            "accelX" to data.accelX,
            "accelY" to data.accelY,
            "accelZ" to data.accelZ,
            "magnitude" to data.magnitude
        )
        sensorService.processSensorData(station, SensorType.VIBRATION, newValues)
    }

    override fun onConnectionLost(station: String) {
        sensorService.markSensorAsDisconnected(station, SensorType.VIBRATION)
    }
}