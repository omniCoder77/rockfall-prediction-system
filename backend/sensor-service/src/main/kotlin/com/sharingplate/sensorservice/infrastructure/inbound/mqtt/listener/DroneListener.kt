package com.sharingplate.sensorservice.infrastructure.inbound.mqtt.listener

import com.google.gson.Gson
import com.sharingplate.sensorservice.domain.contants.SensorType
import com.sharingplate.sensorservice.domain.port.driven.DroneImageDataRepository
import com.sharingplate.sensorservice.infrastructure.inbound.mqtt.SensorService
import com.sharingplate.sensorservice.infrastructure.inbound.mqtt.dto.DroneImageDataEntity
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class DroneListener(private val droneImageDataRepository: DroneImageDataRepository,
                    @Lazy private val sensorService: SensorService
) : MQTTEventListener {
    override fun onReceive(station: String, payload: String) {
        val data = Gson().fromJson(payload, DroneImageDataEntity::class.java)
        droneImageDataRepository.save(station, data.toDomain())
    }

    override fun onConnectionLost(station: String) {
        sensorService.removeSensor(station, SensorType.DRONE)
    }
}