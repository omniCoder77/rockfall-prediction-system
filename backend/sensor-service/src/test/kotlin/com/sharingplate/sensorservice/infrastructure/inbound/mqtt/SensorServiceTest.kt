package com.sharingplate.sensorservice.infrastructure.inbound.mqtt

import com.google.gson.Gson
import com.sharingplate.sensorservice.domain.contants.SensorType
import com.sharingplate.sensorservice.domain.port.driven.DisplacementDataRepository
import com.sharingplate.sensorservice.infrastructure.inbound.mqtt.dto.DisplacementDataEntity
import com.sharingplate.sensorservice.infrastructure.outbound.influxdb.InfluxDBInitializer
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.junit.jupiter.api.*
import org.mockito.Mockito.timeout
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SensorServiceIntegrationTest {

    @Autowired
    private lateinit var sensorService: SensorService

    @MockitoBean
    private lateinit var displacementDataRepository: DisplacementDataRepository

    @Autowired
    private lateinit var influxDBInitializer: InfluxDBInitializer

    private lateinit var testMqttClient: MqttClient
    private val brokerUrl = "tcp://localhost:1883"
    private val testClientId = "TestClient_${System.currentTimeMillis()}"
    private val gson = Gson()

    @BeforeAll
    fun setup() {
        influxDBInitializer.deleteAllDataInBucket()
        testMqttClient = MqttClient(brokerUrl, testClientId, MemoryPersistence())
        val options = MqttConnectOptions().apply {
            isCleanSession = true
            setWill("/health/displacement/station-01", "disconnected".toByteArray(), 2, true)
        }
        testMqttClient.connect(options)
    }

    @AfterAll
    fun tearDown() {
        if (testMqttClient.isConnected) {
            testMqttClient.disconnect()
        }
    }

    @BeforeEach
    fun resetState() {
        // Clear any sensors added in previous tests to ensure isolation
        sensorService.getAllSensors().forEach {
            try {
                sensorService.removeSensor(it.stationId, SensorType.valueOf(it.sensorType))
            } catch (e: Exception) {
                // Ignore errors if sensor already removed
            }
        }
        // Short delay to allow for unsubscribe to complete on the broker
        Thread.sleep(100)
    }

    @Test
    fun `should receive and process message after adding a sensor`() {
        val stationId = "station-1"
        val sensorType = SensorType.DISPLACEMENT

        sensorService.addSensor(stationId, sensorType)
        Thread.sleep(200)

        val displacementData = DisplacementDataEntity(tiltX = 1.0, tiltY = 2.0, temperature = 25.0)
        val payload = gson.toJson(displacementData)
        val topic = "${sensorType.topic}/$stationId"
        val message = MqttMessage(payload.toByteArray())

        testMqttClient.publish(topic, message)

        verify(displacementDataRepository, timeout(1000)).save(any(), any())
    }

    @Test
    fun `should add and list active sensors correctly`() {
        val stationId1 = "station-active-1"
        val sensorType1 = SensorType.DISPLACEMENT
        val stationId2 = "station-active-2"
        val sensorType2 = SensorType.RAINFALL

        sensorService.addSensor(stationId1, sensorType1)
        sensorService.addSensor(stationId2, sensorType2)

        val activeSensors = sensorService.getActiveSensors()

        Assertions.assertEquals(2, activeSensors.size)
        Assertions.assertTrue(activeSensors.any { it.stationId == stationId1 && it.sensorType == sensorType1.name })
        Assertions.assertTrue(activeSensors.any { it.stationId == stationId2 && it.sensorType == sensorType2.name })
    }

    @Test
    fun `should remove sensor and stop receiving messages`() {
        val stationId = "station-remove"
        val sensorType = SensorType.DISPLACEMENT
        val topic = "${sensorType.topic}/$stationId"

        sensorService.addSensor(stationId, sensorType)
        Thread.sleep(100)

        sensorService.removeSensor(stationId, sensorType)
        Thread.sleep(100)

        val displacementData = DisplacementDataEntity(tiltX = 10.0, tiltY = 20.0, temperature = 30.0)
        val payload = gson.toJson(displacementData)
        testMqttClient.publish(topic, MqttMessage(payload.toByteArray()))

        verify(displacementDataRepository, timeout(500).times(0)).save(any(), any())
    }
}