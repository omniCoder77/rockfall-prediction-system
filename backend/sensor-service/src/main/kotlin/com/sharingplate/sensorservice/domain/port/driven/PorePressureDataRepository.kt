package com.sharingplate.sensorservice.domain.port.driven

import com.sharingplate.sensorservice.domain.model.PorePressureData

interface PorePressureDataRepository {
    fun save(station: String, data: PorePressureData)
    fun readByStation(start: String, end: String, station: String?, windowLengthInSeconds: Int): List<PorePressureData>
    fun readByPoints(samplePoints: Int, station: String?): List<PorePressureData>
}