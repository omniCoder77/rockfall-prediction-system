package com.sharingplate.sensorservice.application.service

import com.sharingplate.sensorservice.domain.model.SensorDataPoint
import com.sharingplate.sensorservice.domain.port.driven.SensorHistoryRepository
import org.springframework.stereotype.Service

@Service
class SensorHistoryService(private val sensorHistoryRepository: SensorHistoryRepository) {

    fun getSensorHistory(
        sensorType: String,
        stationId: String?,
        start: String,
        stop: String,
        interval: String
    ): Map<String, List<SensorDataPoint>> = sensorHistoryRepository.getHistory(sensorType, stationId, start, stop, interval)

    fun getAllSensorsHistory(start: String, stop: String, interval: String): Map<String, Map<String, List<SensorDataPoint>>> =
        sensorHistoryRepository.getAllHistory(start, stop, interval)

    fun getStationSensorsHistory(stationId: String, start: String, stop: String, interval: String): Map<String, Map<String, List<SensorDataPoint>>> =
        sensorHistoryRepository.getStationHistory(stationId, start, stop, interval)
}