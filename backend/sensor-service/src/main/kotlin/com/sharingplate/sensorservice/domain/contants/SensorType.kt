package com.sharingplate.sensorservice.domain.contants

enum class SensorType(val topic: String) {
    DISPLACEMENT("/displacement"),
    PORE_PRESSURE("/pore-pressure"),
    TEMPERATURE("temperature"),
    STRAIN("strain"),
    RAINFALL("rainfall"),
    DRONE("drone"),
    VIBRATION("vibration")
}