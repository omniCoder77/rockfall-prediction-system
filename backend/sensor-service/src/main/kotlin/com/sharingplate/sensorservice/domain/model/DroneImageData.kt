package com.sharingplate.sensorservice.domain.model

data class DroneImageData(
    val imageData: ByteArray,
    val lat: Double,
    val long: Double,
    val altitude: Double,
)