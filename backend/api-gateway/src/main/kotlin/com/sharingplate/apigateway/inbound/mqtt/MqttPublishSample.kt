package com.sharingplate.apigateway.inbound.mqtt

import com.google.gson.Gson
import com.sharingplate.apigateway.inbound.mqtt.dto.*
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.time.Instant
import kotlin.random.Random
import kotlin.system.exitProcess

object MqttPublishAllSensors {
    private val gson = Gson()
    private val broker = "tcp://localhost:1883"
    private val qos = 2

    @JvmStatic
    fun main(args: Array<String>) {
        val clientId = "RockfallPredictionPublisher_${System.currentTimeMillis()}"
        val persistence = MemoryPersistence()

        try {
            val sampleClient = MqttClient(broker, clientId, persistence)
            val connOpts = MqttConnectOptions().apply {
                isCleanSession = true
            }

            println("Connecting to broker: $broker")
            sampleClient.connect(connOpts)
            println("Connected")

            // Common station ID for this batch
            val stationId = "STATION_${Random.nextInt(1, 10)}"
            val currentTime = Instant.now().toEpochMilli()

            // Publish displacement data
            publishDisplacementData(sampleClient, stationId, currentTime)

            // Publish strain data
            publishStrainData(sampleClient, stationId, currentTime)

            // Publish pore pressure data
            publishPorePressureData(sampleClient, stationId, currentTime)

            // Publish rainfall data
            publishRainfallData(sampleClient, stationId, currentTime)

            // Publish vibration data
            publishVibrationData(sampleClient, stationId, currentTime)

            // Publish temperature data
            publishTemperatureData(sampleClient, stationId, currentTime)

            // Publish drone image data
            publishDroneImageData(sampleClient, currentTime)

            println("All sensor data published successfully")
            sampleClient.disconnect()
            println("Disconnected")
            exitProcess(0)

        } catch (me: MqttException) {
            println("MQTT Exception:")
            println("reason: ${me.reasonCode}")
            println("msg: ${me.message}")
            println("loc: ${me.localizedMessage}")
            println("cause: ${me.cause}")
            println("exception: $me")
            me.printStackTrace()
        }
    }

    fun publishDisplacementData(client: MqttClient, stationId: String, timestamp: Long) {
        val data = DisplacementData(
            timestamp = timestamp,
            stationId = stationId,
            tiltX = Random.nextDouble(-5.0, 5.0),
            tiltY = Random.nextDouble(-5.0, 5.0),
            temperature = Random.nextDouble(15.0, 35.0),
            batteryLevel = Random.nextInt(60, 100)
        )
        publishMessage(client, "/displacement", data)
    }

    fun publishStrainData(client: MqttClient, stationId: String, timestamp: Long) {
        val data = StrainData(
            timestamp = timestamp,
            stationId = stationId,
            strainValue = Random.nextDouble(-100.0, 100.0),
            temperature = Random.nextDouble(15.0, 35.0),
            frequency = Random.nextDouble(1000.0, 2000.0)
        )
        publishMessage(client, "/strain", data)
    }

    fun publishPorePressureData(client: MqttClient, stationId: String, timestamp: Long) {
        val data = PorePressureData(
            timestamp = timestamp,
            stationId = stationId,
            pressure = Random.nextDouble(50.0, 200.0),
            temperature = Random.nextDouble(15.0, 35.0),
            frequency = Random.nextDouble(1000.0, 2000.0)
        )
        publishMessage(client, "/pressure", data)
    }

    fun publishRainfallData(client: MqttClient, stationId: String, timestamp: Long) {
        val data = RainfallData(
            timestamp = timestamp,
            stationId = stationId,
            rainfallIncrement = Random.nextDouble(0.0, 5.0),
            totalRainfall = Random.nextDouble(0.0, 100.0)
        )
        publishMessage(client, "/rainfall", data)
    }

    fun publishVibrationData(client: MqttClient, stationId: String, timestamp: Long) {
        val data = VibrationData(
            timestamp = timestamp,
            stationId = stationId,
            accelX = Random.nextDouble(-2.0, 2.0),
            accelY = Random.nextDouble(-2.0, 2.0),
            accelZ = Random.nextDouble(-2.0, 2.0),
            magnitude = Random.nextDouble(0.1, 3.0)
        )
        publishMessage(client, "/vibration", data)
    }

    fun publishTemperatureData(client: MqttClient, stationId: String, timestamp: Long) {
        val data = TemperatureData(
            timestamp = timestamp,
            stationId = stationId,
            temperature = Random.nextDouble(15.0, 40.0),
            humidity = Random.nextDouble(30.0, 90.0)
        )
        publishMessage(client, "/temperature", data)
    }

    fun publishDroneImageData(client: MqttClient, timestamp: Long) {
        val data = DroneImageData(
            timestamp = timestamp,
            imageData = generateMockImageData(),
            gpsLatitude = Random.nextDouble(28.0, 29.0), // Delhi region
            gpsLongitude = Random.nextDouble(76.0, 78.0), // Delhi region
            altitude = Random.nextDouble(50.0, 200.0),
            droneId = "DRONE_${Random.nextInt(1, 5)}"
        )
        publishMessage(client, "/droneimagedata", data)
    }

    private fun generateMockImageData(): ByteArray {
        val mockImageSize = 1024 // 1KB mock image
        return ByteArray(mockImageSize) { Random.nextInt(0, 256).toByte() }
    }

    private fun publishMessage(client: MqttClient, topic: String, data: Any) {
        try {
            val jsonContent = gson.toJson(data)
            println("Publishing to $topic: ${if (data is DroneImageData) "DroneImageData (binary content)" else jsonContent}")

            val message = MqttMessage(jsonContent.toByteArray(Charsets.UTF_8)).apply {
                this.qos = this@MqttPublishAllSensors.qos
            }

            client.publish(topic, message)
            println("✓ Published to $topic")

            // Small delay between publications
            Thread.sleep(100)

        } catch (e: Exception) {
            println("✗ Error publishing to $topic: ${e.message}")
        }
    }
}

object MqttContinuousPublisher {
    private val gson = Gson()
    private val broker = "tcp://localhost:1883"
    private val qos = 2

    @JvmStatic
    fun main(args: Array<String>) {
        val clientId = "RockfallContinuousPublisher_${System.currentTimeMillis()}"
        val persistence = MemoryPersistence()

        try {
            val sampleClient = MqttClient(broker, clientId, persistence)
            val connOpts = MqttConnectOptions().apply {
                isCleanSession = true
            }

            println("Connecting to broker for continuous publishing: $broker")
            sampleClient.connect(connOpts)
            println("Connected - Starting continuous publishing (Ctrl+C to stop)")

            // Publish data every 5 seconds
            while (true) {
                val stationId = "STATION_${Random.nextInt(1, 5)}"
                val currentTime = Instant.now().toEpochMilli()

                println("\n--- Publishing batch for $stationId ---")
                publishAllSensorData(sampleClient, stationId, currentTime)

                Thread.sleep(5000) // 5 second interval
            }

        } catch (me: MqttException) {
            println("MQTT Exception: ${me.message}")
            me.printStackTrace()
        } catch (e: InterruptedException) {
            println("Publishing interrupted")
        }
    }

    private fun publishAllSensorData(client: MqttClient, stationId: String, timestamp: Long) {
        MqttPublishAllSensors.run {
            publishDisplacementData(client, stationId, timestamp)
            publishStrainData(client, stationId, timestamp)
            publishPorePressureData(client, stationId, timestamp)
            publishRainfallData(client, stationId, timestamp)
            publishVibrationData(client, stationId, timestamp)
            publishTemperatureData(client, stationId, timestamp)
            publishDroneImageData(client, timestamp)
        }
    }
}