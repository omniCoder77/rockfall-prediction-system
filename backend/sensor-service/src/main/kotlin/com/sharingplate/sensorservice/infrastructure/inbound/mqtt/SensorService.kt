package com.sharingplate.sensorservice.infrastructure.inbound.mqtt

import com.google.gson.Gson
import com.sharingplate.sensorservice.application.config.SensorThresholdsConfig
import com.sharingplate.sensorservice.application.service.SensorEventNotifier
import com.sharingplate.sensorservice.domain.contants.SensorType
import com.sharingplate.sensorservice.infrastructure.inbound.mqtt.listener.MQTTListenerFactory
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
class SensorService(
    private val mQTTListenerFactory: MQTTListenerFactory
) : MqttCallback {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private lateinit var mqttClient: MqttClient
    private val gson = Gson()
    private val brokerUrl = "tcp://localhost:1883"
    private val clientId = "RockfallPredictionClient_${System.currentTimeMillis()}"

    @Autowired
    private lateinit var sensorEventNotifier: SensorEventNotifier
    @Autowired
    private lateinit var thresholdsConfig: SensorThresholdsConfig

    data class SensorInfo(
        val stationId: String, val sensorType: String, var status: String, // "connected" or "disconnected"
        var lastSeen: Long, var lastValues: Map<String, Double> = emptyMap()
    )

    private val allSensors = ConcurrentHashMap<String, SensorInfo>()

    init {
        try {
            mqttClient = MqttClient(brokerUrl, clientId, MemoryPersistence())
            mqttClient.setCallback(this)

            val connectionOptions = MqttConnectOptions().apply {
                isCleanSession = true
                connectionTimeout = 30
                keepAliveInterval = 60
                isAutomaticReconnect = true
            }

            mqttClient.connect(connectionOptions)
            logger.info("Connected to MQTT broker: $brokerUrl with client ID: $clientId")

            // Subscribe to the LWT topic
            mqttClient.subscribe("/health/+/+", 2)
            logger.info("Subscribed to health topic: /health/+/+")

            // Dynamically build and subscribe to topics from the SensorType enum
            SensorType.values().forEach { sensorType ->
                val topic = "${sensorType.topic}/+"
                try {
                    mqttClient.subscribe(topic, 1)
                    logger.info("Subscribed to topic: $topic")
                } catch (e: Exception) {
                    logger.error("Error subscribing to $topic: ${e.message}", e)
                }
            }

        } catch (e: Exception) {
            logger.error("Error connecting to MQTT broker: ${e.message}", e)
        }
    }

    @Synchronized
    fun addSensor(stationId: String, sensorType: SensorType) {
        val topic = "${sensorType.topic}/$stationId"
        if (allSensors.containsKey(topic)) {
            throw IllegalArgumentException("Error: Sensor with topic '$topic' already exists.")
        }
        mqttClient.subscribe(topic)
        val sensorInfo = SensorInfo(stationId, sensorType.name, "connected", System.currentTimeMillis())
        allSensors[topic] = sensorInfo
    }

    @Synchronized
    fun removeSensor(stationId: String, sensorType: SensorType) {
        val topic = "${sensorType.topic}/$stationId"
        mqttClient.unsubscribe(topic)
        allSensors.remove(topic)
    }

    fun getActiveSensors(): List<SensorInfo> {
        return allSensors.values.filter { it.status == "connected" }
    }

    fun getInactiveSensors(): List<SensorInfo> {
        return allSensors.values.filter { it.status == "disconnected" }
    }

    fun getAllSensors(): List<SensorInfo> {
        return allSensors.values.toList()
    }

    @Synchronized
    fun updateSensor(stationId: String, sensorType: SensorType) {
        val topic = "${sensorType.topic}/$stationId"
        val sensorInfo = SensorInfo(stationId, sensorType.name, "connected", System.currentTimeMillis())
        allSensors[topic] = sensorInfo
        mqttClient.subscribe(topic)
    }

    override fun connectionLost(cause: Throwable?) {
        logger.warn("Connection to MQTT broker lost: ${cause?.message}")
    }

    override fun messageArrived(topic: String, message: MqttMessage) {
        try {
            val parts = topic.removePrefix("/").split("/")
            if (parts.isEmpty()) return

            when (parts[0].lowercase()) {
                "health" -> handleHealthMessage(parts)
                else -> handleSensorMessage(topic, message)
            }
        } catch (e: Exception) {
            logger.error("Failed to process message on topic '$topic': ${e.message}", e)
        }
    }

    private fun handleHealthMessage(parts: List<String>) {
        if (parts.size < 3) {
            logger.warn("Ignoring malformed health topic: /$parts")
            return
        }
        val sensorTypeName = parts[1].uppercase()
        val stationId = parts[2]

        try {
            val sensorType = SensorType.valueOf(sensorTypeName)
            logger.warn("Received LWT for sensor $sensorType at station $stationId. Marking as disconnected.")
            mQTTListenerFactory.getListener(sensorType).onConnectionLost(stationId)
        } catch (e: IllegalArgumentException) {
            logger.error("Received health message for unknown sensor type: $sensorTypeName")
        }
    }

    private fun handleSensorMessage(topic: String, message: MqttMessage) {
        val sensorType = SensorType.values().find { topic.startsWith(it.topic) } ?: return

        val stationId = topic.substringAfter(sensorType.topic + "/")
        if (stationId.isEmpty() || stationId.contains('/')) return // Ignore malformed topics

        val payload = String(message.payload)
        logger.debug("Message received on topic: $topic - Payload: $payload")
        mQTTListenerFactory.getListener(sensorType).onReceive(stationId, payload)
    }


    override fun deliveryComplete(token: IMqttDeliveryToken?) {}

    @EventListener(ContextClosedEvent::class)
    fun disconnect() {
        if (::mqttClient.isInitialized && mqttClient.isConnected) {
            mqttClient.disconnect()
            logger.info("Disconnected from MQTT broker")
        }
    }

    @Synchronized
    fun markSensorAsDisconnected(stationId: String, sensorType: SensorType) {
        val topic = "${sensorType.topic}/$stationId"
        allSensors[topic]?.let {
            it.status = "disconnected"
            logger.warn("Sensor $sensorType at station $stationId marked as disconnected.")
            sensorEventNotifier.notifySensorDown(stationId, sensorType)
        }
    }

    @Synchronized
    fun processSensorData(stationId: String, sensorType: SensorType, newValues: Map<String, Double>) {
        val topic = "${sensorType.topic}/$stationId"
        val sensorInfo = allSensors.computeIfAbsent(topic) {
            SensorInfo(stationId, sensorType.name, "connected", System.currentTimeMillis())
        }

        val lastValues = sensorInfo.lastValues
        val sensorThresholds = thresholdsConfig.thresholds[sensorType.name]

        newValues.forEach { (field, currentValue) ->
            val previousValue = lastValues[field]
            val threshold = sensorThresholds?.get(field)

            if (previousValue != null && threshold != null) {
                if (abs(currentValue - previousValue) > threshold) {
                    logger.info("Fluctuation detected for sensor $sensorType at station $stationId, field '$field'. From $previousValue to $currentValue")
                    sensorEventNotifier.notifySensorFluctuation(stationId, sensorType, field, previousValue, currentValue)
                }
            }
        }

        sensorInfo.lastValues = sensorInfo.lastValues + newValues
        sensorInfo.lastSeen = System.currentTimeMillis()
        sensorInfo.status = "connected"
    }
}