package com.sharingplate.sensorservice.domain.port.driven

import com.sharingplate.sensorservice.domain.model.DroneImageData

interface DroneImageDataRepository {
    fun save(station: String, data: DroneImageData)
    fun readByStation(start: String, end: String, station: String?, windowLengthInSeconds: Int): List<DroneImageData>
    fun readByPoints(samplePoints: Int, station: String?): List<DroneImageData>
}