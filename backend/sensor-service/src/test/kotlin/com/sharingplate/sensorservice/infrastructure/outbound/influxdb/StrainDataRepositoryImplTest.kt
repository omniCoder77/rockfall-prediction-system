package com.sharingplate.sensorservice.infrastructure.outbound.influxdb

import com.sharingplate.sensorservice.domain.model.StrainData
import org.junit.jupiter.api.*
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StrainDataRepositoryImplTest {

    private lateinit var influxDBInitializer: InfluxDBInitializer
    private lateinit var repository: StrainDataRepositoryImpl

    @BeforeAll
    fun setup() {
        influxDBInitializer = InfluxDBInitializer(
            databaseURL = "http://localhost:8086",
            token = "c98kUKxwTWvoO58UlHCNa0YJLqDzX8PjT7tQIUEA5nxLU3Dq1nw1uOuHr23QftO41qyc-ci_gtHZmpFHwQs9dA==",
            bucketName = "MineProtector",
            organization = "MineProtector"
        )
        repository = StrainDataRepositoryImpl(influxDBInitializer)
    }

    @BeforeEach
    fun cleanBucket() {
        influxDBInitializer.deleteAllDataInBucket()
        Thread.sleep(1000)
    }

    @Test
    fun `save and readByStation returns saved data`() {
        val now = OffsetDateTime.now()
        val data = StrainData(System.currentTimeMillis(), 150.7, 28.2, 60.0)
        repository.save("stationA", data)

        Thread.sleep(1500)

        val start = now.minusMinutes(5).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val end = now.plusMinutes(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        val result = repository.readByStation(start, end, "stationA", 60)

        Assertions.assertTrue(result.isNotEmpty(), "Expected at least one record")
        val rec = result.first()
        Assertions.assertEquals(150.7, rec.strainValue, 0.0001)
        Assertions.assertEquals(28.2, rec.temperature, 0.0001)
        Assertions.assertEquals(60.0, rec.frequency, 0.0001)
    }

    @Test
    fun `readByPoints returns last n points`() {
        (1..5).forEach {
            repository.save("stationC", StrainData(System.currentTimeMillis(), 150.0 + it, 25.0, 60.0))
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