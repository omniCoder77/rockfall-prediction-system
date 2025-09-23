package com.sharingplate.sensorservice.domain.port.driven

import com.sharingplate.sensorservice.domain.model.DisplacementData

interface DisplacementDataRepository {
    fun save(data: DisplacementData, station: String)
    fun readByStation(start: String, end: String, station: String?, windowLengthInSeconds: Int): List<DisplacementData>
    fun readByPoints(samplePoints: Int, station: String?): List<DisplacementData>
}