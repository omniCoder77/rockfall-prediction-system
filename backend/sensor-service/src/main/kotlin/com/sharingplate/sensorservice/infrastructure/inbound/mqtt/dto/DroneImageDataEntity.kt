package com.sharingplate.sensorservice.infrastructure.inbound.mqtt.dto

import com.sharingplate.sensorservice.domain.model.DroneImageData

data class DroneImageDataEntity(
    val imageData: ByteArray,
    val gpsLatitude: Double,
    val gpsLongitude: Double,
    val altitude: Double,
) {
    fun toDomain() = DroneImageData(
        imageData = this.imageData,
        lat = this.gpsLatitude,
        long = this.gpsLongitude,
        altitude = this.altitude,
    )
}