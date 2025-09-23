package com.sharingplate.sensorservice.application.service

import com.sharingplate.sensorservice.domain.contants.SensorType
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.time.Instant

data class SensorStatusEvent(
    val stationId: String,
    val sensorType: String,
    val status: String,
    val timestamp: Long = Instant.now().toEpochMilli()
)

data class SensorFluctuationEvent(
    val stationId: String,
    val sensorType: String,
    val field: String,
    val previousValue: Double,
    val currentValue: Double,
    val timestamp: Long = Instant.now().toEpochMilli()
)

@Service
class SensorEventNotifier(private val kafkaTemplate: KafkaTemplate<String, Any>) {
    private val sensorStatusTopic = "sensor-status-events"
    private val sensorFluctuationTopic = "sensor-fluctuation-events"

    fun notifySensorDown(stationId: String, sensorType: SensorType) {
        val event = SensorStatusEvent(stationId, sensorType.name, "DOWN")
        kafkaTemplate.send(sensorStatusTopic, event)
    }

    fun notifySensorFluctuation(
        stationId: String,
        sensorType: SensorType,
        field: String,
        previousValue: Double,
        currentValue: Double
    ) {
        val event = SensorFluctuationEvent(stationId, sensorType.name, field, previousValue, currentValue)
        kafkaTemplate.send(sensorFluctuationTopic, event)
    }
}