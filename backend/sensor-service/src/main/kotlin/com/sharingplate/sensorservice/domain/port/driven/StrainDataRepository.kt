package com.sharingplate.sensorservice.domain.port.driven

import com.sharingplate.sensorservice.domain.model.StrainData

interface StrainDataRepository {
    fun save(station: String, data: StrainData)
    fun readByStation(start: String, end: String, station: String?, windowLengthInSeconds: Int): List<StrainData>
    fun readByPoints(samplePoints: Int, station: String?): List<StrainData>
}