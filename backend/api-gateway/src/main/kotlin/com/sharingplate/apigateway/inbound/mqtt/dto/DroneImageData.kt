package com.sharingplate.apigateway.inbound.mqtt.dto

data class DroneImageData(
    val timestamp: Long,
    val imageData: ByteArray,
    val gpsLatitude: Double,
    val gpsLongitude: Double,
    val altitude: Double,
    val droneId: String
)