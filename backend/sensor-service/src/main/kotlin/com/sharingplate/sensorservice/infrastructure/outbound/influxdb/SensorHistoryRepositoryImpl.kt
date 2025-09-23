package com.sharingplate.sensorservice.infrastructure.outbound.influxdb

import com.influxdb.query.FluxRecord
import com.sharingplate.sensorservice.domain.model.SensorDataPoint
import com.sharingplate.sensorservice.domain.port.driven.SensorHistoryRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class SensorHistoryRepositoryImpl(private val influxDBInitializer: InfluxDBInitializer) : SensorHistoryRepository {

    private val sensorMeasurements = listOf(
        "displacement", "pore_pressure", "temperature", "strain",
        "rainfall", "drone_image_data", "vibration"
    )

    override fun getHistory(
        measurement: String,
        stationId: String?,
        start: String,
        stop: String,
        interval: String
    ): Map<String, List<SensorDataPoint>> {
        val stationFilter = if (stationId != null) {
            """|> filter(fn: (r) => r["station"] == "$stationId")"""
        } else {
            ""
        }

        val fluxQuery = """
            from(bucket: "${influxDBInitializer.bucketName}")
              |> range(start: $start, stop: $stop)
              |> filter(fn: (r) => r["_measurement"] == "$measurement")
              $stationFilter
              |> filter(fn: (r) => r["_field"] != "station")
              |> aggregateWindow(every: ${interval}, fn: mean, createEmpty: false)
              |> yield(name: "mean")
        """.trimIndent()

        val queryApi = influxDBInitializer.queryApi
        val tables = queryApi.query(fluxQuery)
        val resultMap = mutableMapOf<String, MutableList<SensorDataPoint>>()

        for (table in tables) {
            for (record in table.records) {
                val dataPoint = mapToSensorDataPoint(record)
                resultMap.computeIfAbsent(dataPoint.field) { mutableListOf() }.add(dataPoint)
            }
        }
        return resultMap
    }

    override fun getAllHistory(start: String, stop: String, interval: String): Map<String, Map<String, List<SensorDataPoint>>> {
        val measurementFilter = sensorMeasurements.joinToString(separator = " or ") { """r["_measurement"] == "$it"""" }

        val fluxQuery = """
            from(bucket: "${influxDBInitializer.bucketName}")
              |> range(start: $start, stop: $stop)
              |> filter(fn: (r) => $measurementFilter)
              |> filter(fn: (r) => r["_field"] != "station")
              |> aggregateWindow(every: ${interval}, fn: mean, createEmpty: false)
              |> yield(name: "mean")
        """.trimIndent()
        return executeQueryAndGroup(fluxQuery)
    }

    override fun getStationHistory(stationId: String, start: String, stop: String, interval: String): Map<String, Map<String, List<SensorDataPoint>>> {
        val measurementFilter = sensorMeasurements.joinToString(separator = " or ") { """r["_measurement"] == "$it"""" }

        val fluxQuery = """
            from(bucket: "${influxDBInitializer.bucketName}")
              |> range(start: $start, stop: $stop)
              |> filter(fn: (r) => r["station"] == "$stationId")
              |> filter(fn: (r) => $measurementFilter)
              |> filter(fn: (r) => r["_field"] != "station")
              |> aggregateWindow(every: ${interval}, fn: mean, createEmpty: false)
              |> yield(name: "mean")
        """.trimIndent()
        return executeQueryAndGroup(fluxQuery)
    }

    private fun executeQueryAndGroup(fluxQuery: String): Map<String, Map<String, List<SensorDataPoint>>> {
        val queryApi = influxDBInitializer.queryApi
        val tables = queryApi.query(fluxQuery)
        val resultMap = mutableMapOf<String, MutableMap<String, MutableList<SensorDataPoint>>>()

        for (table in tables) {
            for (record in table.records) {
                val dataPoint = mapToSensorDataPoint(record)
                resultMap.computeIfAbsent(dataPoint.measurement) { mutableMapOf() }
                    .computeIfAbsent(dataPoint.field) { mutableListOf() }
                    .add(dataPoint)
            }
        }
        return resultMap
    }

    private fun mapToSensorDataPoint(record: FluxRecord): SensorDataPoint {
        return SensorDataPoint(
            time = record.time ?: Instant.now(),
            value = (record.value as? Double) ?: 0.0,
            measurement = record.measurement ?: "",
            field = record.field ?: "",
            station = record.values["station"]?.toString() ?: ""
        )
    }
}