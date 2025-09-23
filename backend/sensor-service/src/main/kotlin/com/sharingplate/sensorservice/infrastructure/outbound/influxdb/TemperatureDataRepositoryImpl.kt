package com.sharingplate.sensorservice.infrastructure.outbound.influxdb

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import com.influxdb.query.FluxRecord
import com.sharingplate.sensorservice.domain.model.TemperatureData
import com.sharingplate.sensorservice.domain.port.driven.TemperatureDataRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class TemperatureDataRepositoryImpl(private val influxDBInitializer: InfluxDBInitializer) : TemperatureDataRepository {
    override fun save(
        station: String,
        data: TemperatureData
    ) {
        val point = Point("temperature")
            .addTag("station", station)
            .addField("temperature", data.temperature)
            .addField("humidity", data.humidity)
            .time(Instant.now(), WritePrecision.NS)
        influxDBInitializer.influxDbWriter.writePoint(point)
    }

    override fun readByStation(
        start: String,
        end: String,
        station: String?,
        windowLengthInSeconds: Int
    ): List<TemperatureData> {
        val stationFilter = if (station != null) """|> filter(fn: (r) => r["station"] == "$station")""" else ""
        val fluxQuery = """
            from(bucket: "${influxDBInitializer.bucketName}")
              |> range(start: $start, stop: $end)
              |> filter(fn: (r) => r["_measurement"] == "temperature")
              $stationFilter
              |> aggregateWindow(every: ${windowLengthInSeconds}s, fn: mean, createEmpty: false)
              |> pivot(rowKey:["_time"], columnKey: ["_field"], valueColumn: "_value")
        """.trimIndent()
        return executeQuery(fluxQuery)
    }

    override fun readByPoints(samplePoints: Int, station: String?): List<TemperatureData> {
        val stationFilter = if (station != null) """|> filter(fn: (r) => r["station"] == "$station")""" else ""
        val fluxQuery = """
            from(bucket: "${influxDBInitializer.bucketName}")
              |> range(start: 0)
              |> filter(fn: (r) => r["_measurement"] == "temperature")
              $stationFilter
              |> pivot(rowKey:["_time"], columnKey: ["_field"], valueColumn: "_value")
              |> sort(columns: ["_time"], desc: true)
              |> limit(n: $samplePoints)
        """.trimIndent()
        return executeQuery(fluxQuery)
    }

    private fun executeQuery(fluxQuery: String): List<TemperatureData> {
        val tables = influxDBInitializer.queryApi.query(fluxQuery)
        val dataList = mutableListOf<TemperatureData>()
        for (table in tables) {
            for (record in table.records) {
                dataList.add(mapToTemperatureData(record))
            }
        }
        return dataList
    }

    private fun mapToTemperatureData(record: FluxRecord): TemperatureData {
        return TemperatureData(
            timestamp = record.time?.toEpochMilli() ?: 0,
            temperature = (record.getValueByKey("temperature") as? Double) ?: 0.0,
            humidity = (record.getValueByKey("humidity") as? Double) ?: 0.0
        )
    }
}