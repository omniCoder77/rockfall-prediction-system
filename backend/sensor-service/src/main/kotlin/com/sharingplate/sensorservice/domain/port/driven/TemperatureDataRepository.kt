package com.sharingplate.sensorservice.domain.port.driven

import com.sharingplate.sensorservice.domain.model.TemperatureData

interface TemperatureDataRepository {
    fun save(station: String, data: TemperatureData)
    fun readByStation(start: String, end: String, station: String?, windowLengthInSeconds: Int): List<TemperatureData>
    fun readByPoints(samplePoints: Int, station: String?): List<TemperatureData>
}