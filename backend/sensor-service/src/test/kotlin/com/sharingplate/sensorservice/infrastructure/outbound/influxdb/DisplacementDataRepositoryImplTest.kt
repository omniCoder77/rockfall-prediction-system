package com.sharingplate.sensorservice.infrastructure.outbound.influxdb

import com.sharingplate.sensorservice.domain.model.DisplacementData
import org.junit.jupiter.api.*
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DisplacementDataRepositoryImplIntegrationTest {

    private lateinit var influxDBInitializer: InfluxDBInitializer
    private lateinit var repository: DisplacementDataRepositoryImpl

    private val logger = LoggerFactory.getLogger(this::class.java)

    @BeforeAll
    fun setup() {
        influxDBInitializer = InfluxDBInitializer(
            databaseURL = "http://localhost:8086",
            token = "c98kUKxwTWvoO58UlHCNa0YJLqDzX8PjT7tQIUEA5nxLU3Dq1nw1uOuHr23QftO41qyc-ci_gtHZmpFHwQs9dA==",
            bucketName = "MineProtector",
            organization = "MineProtector"
        )
        repository = DisplacementDataRepositoryImpl(influxDBInitializer)
    }

    @BeforeEach
    fun cleanBucket() {
        influxDBInitializer.deleteAllDataInBucket()
        Thread.sleep(1000)
    }

    @Test
    fun `save and readByStation returns saved data`() {
        val now = OffsetDateTime.now()

        val data = DisplacementData(tiltX = 10.5, tiltY = 20.5, temperature = 30.0)
        repository.save(data, "stationA")

        Thread.sleep(1500)

        val start = now.minusMinutes(5).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val end = now.plusMinutes(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        val result = repository.readByStation(start, end, "stationA", 60)

        Assertions.assertTrue(result.isNotEmpty(), "Expected at least one record")
        val rec = result.first()
        Assertions.assertEquals(10.5, rec.tiltX, 0.0001)
        Assertions.assertEquals(20.5, rec.tiltY, 0.0001)
        Assertions.assertEquals(30.0, rec.temperature, 0.0001)
    }

    @Test
    fun `save multiple stations and readByStation filters correctly`() {
        val data1 = DisplacementData(1.0, 2.0, 3.0)
        val data2 = DisplacementData(4.0, 5.0, 6.0)
        repository.save(data1, "stationA")
        repository.save(data2, "stationB")

        Thread.sleep(1500)

        val start = OffsetDateTime.now().minusMinutes(5).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val end = OffsetDateTime.now().plusMinutes(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        val onlyA = repository.readByStation(start, end, "stationA", 60)
        Assertions.assertTrue(onlyA.all { it.tiltX == 1.0 })

        val all = repository.readByStation(start, end, null, 60)
        Assertions.assertTrue(all.size >= 2)
    }

    @Test
    fun `readByPoints returns last n points`() {
        (1..5).forEach {
            repository.save(DisplacementData(it.toDouble(), it.toDouble() + 1, 10.0 + it), "stationC")
        }

        Thread.sleep(1500)

        val result = repository.readByPoints(3, "stationC")
        Assertions.assertEquals(3, result.size)
    }

    @Test
    fun `handles empty bucket gracefully`() {
        val start = OffsetDateTime.now().minusHours(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val end = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val result = repository.readByStation(start, end, "unknown", 60)
        Assertions.assertTrue(result.isEmpty(), "Expected no records")
    }
}
