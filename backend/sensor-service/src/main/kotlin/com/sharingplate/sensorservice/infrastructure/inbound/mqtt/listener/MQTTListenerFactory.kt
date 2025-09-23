package com.sharingplate.sensorservice.infrastructure.inbound.mqtt.listener

import com.sharingplate.sensorservice.domain.contants.SensorType
import org.springframework.stereotype.Component

@Component
class MQTTListenerFactory(
    private val displacementListener: DisplacementListener,
    private val porePressureListener: PorePressureListener,
    private val temperatureListener: TemperatureListener,
    private val strainListener: StrainListener,
    private val rainfallListener: RainfallListener,
    private val droneListener: DroneListener,
    private val vibrationListener: VibrationListener
) {
    fun getListener(sensorType: SensorType): MQTTEventListener {
        return when (sensorType) {
            SensorType.DISPLACEMENT -> displacementListener
            SensorType.PORE_PRESSURE -> porePressureListener
            SensorType.TEMPERATURE -> temperatureListener
            SensorType.STRAIN -> strainListener
            SensorType.RAINFALL -> rainfallListener
            SensorType.DRONE -> droneListener
            SensorType.VIBRATION -> vibrationListener
        }
    }
}