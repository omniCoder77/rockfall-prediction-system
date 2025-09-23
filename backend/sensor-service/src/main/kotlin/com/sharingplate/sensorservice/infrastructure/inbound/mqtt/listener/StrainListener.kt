package com.sharingplate.sensorservice.infrastructure.inbound.mqtt.listener

import com.google.gson.Gson
import com.sharingplate.sensorservice.domain.contants.SensorType
import com.sharingplate.sensorservice.domain.port.driven.StrainDataRepository
import com.sharingplate.sensorservice.infrastructure.inbound.mqtt.SensorService
import com.sharingplate.sensorservice.infrastructure.inbound.mqtt.dto.StrainDataEntity
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
class StrainListener(@Lazy private val sensorService: SensorService,
                     private val strainDataRepository: StrainDataRepository
) : MQTTEventListener {
    override fun onReceive(station: String, payload: String) {
        val data = Gson().fromJson(payload, StrainDataEntity::class.java)
        strainDataRepository.save(station, data.toDomain())
    }

    override fun onConnectionLost(station: String) {
        sensorService.removeSensor(station, SensorType.STRAIN)
    }
}