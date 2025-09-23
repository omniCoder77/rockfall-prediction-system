package com.sharingplate.sensorservice.infrastructure.inbound.mqtt.listener

interface MQTTEventListener {
    fun onReceive(station: String, payload: String)
    fun onConnectionLost(station: String)
}