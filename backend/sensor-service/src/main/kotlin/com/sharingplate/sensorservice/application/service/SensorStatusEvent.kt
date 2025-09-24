package com.sharingplate.sensorservice.application.service

import com.google.gson.Gson
import com.sharingplate.sensorservice.domain.contants.SensorType
import com.sharingplate.sensorservice.domain.port.driven.*
import com.sharingplate.sensorservice.infrastructure.outbound.influxdb.StrainDataRepositoryImpl
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
    val timestamp: Long = Instant.now().toEpochMilli(),
    val stationId: String,
    val lat: Double,
    val lon: Double,
    val elevationM: Double,
    val totalRainfallMm: Double,
    val dispIncMm_x: Double,
    val dispIncMm_y: Double,
    val strainIncMicro: Double,
    val poreKpa: Double,
    val vibration: Double,
    val sensorType: String,
    val field: String,
    val previousValue: Double,
    val currentValue: Double
)

@Service
class SensorEventNotifier(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val displacementDataRepository: DisplacementDataRepository,
    private val droneImageDataRepository: DroneImageDataRepository,
    private val rainfallDataRepository: RainfallDataRepository,
    private val strainDataRepositoryImpl: StrainDataRepositoryImpl,
    private val vibrationDataRepository: VibrationDataRepository,
    private val porePressureDataRepository: PorePressureDataRepository
) {
    private val sensorStatusTopic = "sensor-status-events"
    private val sensorFluctuationTopic = "sensor-fluctuation-events"

    fun notifySensorDown(stationId: String, sensorType: SensorType) {
        val event = SensorStatusEvent(stationId, sensorType.name, "DOWN")
        kafkaTemplate.send(sensorStatusTopic, event)
    }

    fun notifySensorFluctuation(
        stationId: String, sensorType: SensorType, field: String, previousValue: Double, currentValue: Double
    ) {
        val displacement = displacementDataRepository.readByPoints(1, stationId).first()
        val drone = droneImageDataRepository.readByPoints(1, stationId).first()
        val rainfall = rainfallDataRepository.readByPoints(1, stationId).first()
        val strain = strainDataRepositoryImpl.readByPoints(1, stationId).first()
        val vibration = vibrationDataRepository.readByPoints(1, stationId).first()
        val pressure = porePressureDataRepository.readByPoints(1, stationId).first()
        val event = SensorFluctuationEvent(
            stationId = stationId,
            sensorType = sensorType.name,
            field = field,
            lat = drone.lat,
            lon = drone.long,
            totalRainfallMm = rainfall.totalRainfall,
            strainIncMicro = strain.strainValue,
            vibration = vibration.magnitude,
            elevationM = drone.altitude,
            dispIncMm_x = displacement.tiltX,
            dispIncMm_y = displacement.tiltY,
            poreKpa = pressure.pressure,
            previousValue = previousValue,
            currentValue = currentValue
        )
        val data = Gson().toJson(event)
        kafkaTemplate.send(sensorFluctuationTopic, data)
    }
}