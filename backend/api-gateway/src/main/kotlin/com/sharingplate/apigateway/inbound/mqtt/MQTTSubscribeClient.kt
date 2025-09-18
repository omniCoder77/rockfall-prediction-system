package com.sharingplate.apigateway.inbound.mqtt

import com.google.gson.Gson
import com.sharingplate.apigateway.inbound.mqtt.dto.*
import com.sharingplate.apigateway.infrastructure.persistence.influxdb.InfluxDBInitializer
import jakarta.annotation.PostConstruct
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.concurrent.thread

@Component
class MqttSensorSubscriber(private val influxDBInitializer: InfluxDBInitializer) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private lateinit var mqttClient: MqttClient
    private val gson = Gson()
    private val brokerUrl = "tcp://localhost:1883"
    private val clientId = "RockfallPredictionClient"

    private val topics = arrayOf(
        "/displacement", "/strain", "/pressure", "/rainfall", "/vibration", "/temperature", "/droneimagedata"
    )

    @PostConstruct
    fun init() {
        try {
            mqttClient = MqttClient(brokerUrl, clientId, MemoryPersistence())

            val connectionOptions = MqttConnectOptions().apply {
                isCleanSession = true
                connectionTimeout = 30
                keepAliveInterval = 60
            }

            mqttClient.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    logger.info("Connection lost: ${cause?.message}")
                    // Implement reconnection logic here
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    handleIncomingMessage(topic, message)
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    // Not needed for subscriber
                }
            })

            mqttClient.connect(connectionOptions)
            logger.info("Connected to MQTT broker: $brokerUrl")

            // Subscribe to all topics
            subscribeToTopics()

        } catch (e: Exception) {
            logger.error("Error connecting to MQTT broker: ${e.message}")
        }
    }

    private fun subscribeToTopics() {
        topics.forEach { topic ->
            try {
                mqttClient.subscribe(topic, 1)
                logger.info("Subscribed to topic: $topic")
            } catch (e: Exception) {
                logger.error("Error subscribing to $topic: ${e.message}")
            }
        }
    }

    private fun handleIncomingMessage(topic: String?, message: MqttMessage?) {
        if (topic == null || message == null) return

        val payload = String(message.payload)

        try {
            when (topic) {
                "/displacement" -> {
                    val data = gson.fromJson(payload, DisplacementData::class.java)
                    processDisplacementData(data)
                }

                "/strain" -> {
                    val data = gson.fromJson(payload, StrainData::class.java)
                    processStrainData(data)
                }

                "/pressure" -> {
                    val data = gson.fromJson(payload, PorePressureData::class.java)
                    processPorePressureData(data)
                }

                "/rainfall" -> {
                    val data = gson.fromJson(payload, RainfallData::class.java)
                    processRainfallData(data)
                }

                "/vibration" -> {
                    val data = gson.fromJson(payload, VibrationData::class.java)
                    processVibrationData(data)
                }

                "/temperature" -> {
                    val data = gson.fromJson(payload, TemperatureData::class.java)
                    processTemperatureData(data)
                }

                "/droneimagedata" -> {
                    val data = gson.fromJson(payload, DroneImageData::class.java)
                    processDroneImageData(data)
                }

                else -> {
                    println("Unknown topic: $topic")
                }
            }
        } catch (e: Exception) {
            println("Error parsing message from $topic: ${e.message}")
        }
    }

    private fun processDisplacementData(data: DisplacementData) {
        influxDBInitializer.addDisplacementData(data)
    }

    private fun processStrainData(data: StrainData) {
        influxDBInitializer.addStrainData(data)
    }

    private fun processPorePressureData(data: PorePressureData) {
        influxDBInitializer.addPressureData(data)
    }

    private fun processRainfallData(data: RainfallData) {
        influxDBInitializer.addRainfallData(data)
    }

    private fun processVibrationData(data: VibrationData) {
        influxDBInitializer.addVibrationData(data)
    }

    private fun processTemperatureData(data: TemperatureData) {
        influxDBInitializer.addTemperatureData(data)
    }

    private fun processDroneImageData(data: DroneImageData) {
        influxDBInitializer.addDroneData(data)
    }

    fun disconnect() {
        if (::mqttClient.isInitialized && mqttClient.isConnected) {
            mqttClient.disconnect()
            logger.info("Disconnected from MQTT broker")
        }
    }
}

fun main() {
    val influx =         InfluxDBInitializer(
        "http://localhost:8086",
        "c98kUKxwTWvoO58UlHCNa0YJLqDzX8PjT7tQIUEA5nxLU3Dq1nw1uOuHr23QftO41qyc-ci_gtHZmpFHwQs9dA==",
        "MineProtector",
        "MineProtector"
    )

    val subscriber = MqttSensorSubscriber(influx)
    subscriber.init()
    thread(start = true) {
        while (true) {
            influx.writeBatchData()
            Thread.sleep(3000)
        }
    }
}