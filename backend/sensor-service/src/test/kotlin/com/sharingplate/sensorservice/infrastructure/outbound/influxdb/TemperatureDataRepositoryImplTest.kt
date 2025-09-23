package com.sharingplate.sensorservice.infrastructure.outbound.influxdb

import com.sharingplate.sensorservice.domain.model.TemperatureData
import org.junit.jupiter.api.*
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TemperatureDataRepositoryImplTest {

    private lateinit var influxDBInitializer: InfluxDBInitializer
    private lateinit var repository: TemperatureDataRepositoryImpl

    @BeforeAll
    fun setup() {
        influxDBInitializer = InfluxDBInitializer(
            databaseURL = "http://localhost:8086",
            token = "c98kUKxwTWvoO58UlHCNa0YJLqDzX8PjT7tQIUEA5nxLU3Dq1nw1uOuHr23QftO41qyc-ci_gtHZmpFHwQs9dA==",
            bucketName = "MineProtector",
            organization = "MineProtector"
        )
        repository = TemperatureDataRepositoryImpl(influxDBInitializer)
    }

    @BeforeEach
    fun cleanBucket() {
        influxDBInitializer.deleteAllDataInBucket()
        Thread.sleep(1000)
    }

    @Test
    fun `save and readByStation returns saved data`() {
        val now = OffsetDateTime.now()
        val data = TemperatureData(timestamp = System.currentTimeMillis(), temperature = 25.5, humidity = 60.2)
        repository.save("stationA", data)

        Thread.sleep(1500)

        val start = now.minusMinutes(5).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val end = now.plusMinutes(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        val result = repository.readByStation(start, end, "stationA", 60)

        Assertions.assertTrue(result.isNotEmpty(), "Expected at least one record")
        val rec = result.first()
        Assertions.assertEquals(25.5, rec.temperature, 0.0001)
        Assertions.assertEquals(60.2, rec.humidity, 0.0001)
    }

    @Test
    fun `save multiple stations and readByStation filters correctly`() {
        val data1 = TemperatureData(System.currentTimeMillis(), 22.0, 55.0)
        val data2 = TemperatureData(System.currentTimeMillis(), 28.0, 65.0)
        repository.save("stationA", data1)
        repository.save("stationB", data2)

        Thread.sleep(1500)

        val start = OffsetDateTime.now().minusMinutes(5).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val end = OffsetDateTime.now().plusMinutes(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        val onlyA = repository.readByStation(start, end, "stationA", 60)
        Assertions.assertTrue(onlyA.isNotEmpty())
        Assertions.assertTrue(onlyA.all { it.temperature == 22.0 })

        val all = repository.readByStation(start, end, null, 60)
        Assertions.assertTrue(all.size >= 2)
    }

    @Test
    fun `readByPoints returns last n points`() {
        (1..5).forEach {
            repository.save("stationC", TemperatureData(System.currentTimeMillis(), it.toDouble(), 50.0 + it))
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