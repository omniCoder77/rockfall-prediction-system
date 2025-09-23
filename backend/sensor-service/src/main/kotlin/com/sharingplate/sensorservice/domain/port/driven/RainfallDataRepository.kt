package com.sharingplate.sensorservice.domain.port.driven

import com.sharingplate.sensorservice.domain.model.RainfallData

interface RainfallDataRepository {
    fun save(station: String, data: RainfallData)
    fun readByStation(start: String, end: String, station: String?, windowLengthInSeconds: Int): List<RainfallData>
    fun readByPoints(samplePoints: Int, station: String?): List<RainfallData>
}