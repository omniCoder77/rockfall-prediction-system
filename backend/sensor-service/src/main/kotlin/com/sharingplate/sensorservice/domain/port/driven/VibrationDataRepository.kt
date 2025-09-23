package com.sharingplate.sensorservice.domain.port.driven

import com.sharingplate.sensorservice.domain.model.VibrationData

interface VibrationDataRepository {
    fun save(station: String, data: VibrationData)
    fun readByStation(start: String, end: String, station: String?, windowLengthInSeconds: Int): List<VibrationData>
    fun readByPoints(samplePoints: Int, station: String?): List<VibrationData>
}