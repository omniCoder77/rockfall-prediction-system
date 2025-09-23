package com.sharingplate.sensorservice.infrastructure.outbound.influxdb

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import com.influxdb.query.FluxRecord
import com.sharingplate.sensorservice.domain.model.RainfallData
import com.sharingplate.sensorservice.domain.port.driven.RainfallDataRepository
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class RainfallDataRepositoryImpl(private val influxDBInitializer: InfluxDBInitializer) : RainfallDataRepository {
    override fun save(station: String, data: RainfallData) {
        val point = Point("rainfall")
            .addTag("station", station)
            .addField("rainfall_increment", data.rainfallIncrement)
            .addField("total_rainfall", data.totalRainfall)
            .time(Instant.now(), WritePrecision.NS)
        influxDBInitializer.influxDbWriter.writePoint(point)
    }

    override fun readByStation(
        start: String,
        end: String,
        station: String?,
        windowLengthInSeconds: Int
    ): List<RainfallData> {
        val stationFilter = if (station != null) """|> filter(fn: (r) => r["station"] == "$station")""" else ""
        val fluxQuery = """
            from(bucket: "${influxDBInitializer.bucketName}")
              |> range(start: $start, stop: $end)
              |> filter(fn: (r) => r["_measurement"] == "rainfall")
              $stationFilter
              |> aggregateWindow(every: ${windowLengthInSeconds}s, fn: mean, createEmpty: false)
              |> pivot(rowKey:["_time"], columnKey: ["_field"], valueColumn: "_value")
        """.trimIndent()
        return executeQuery(fluxQuery)
    }

    override fun readByPoints(samplePoints: Int, station: String?): List<RainfallData> {
        val stationFilter = if (station != null) """|> filter(fn: (r) => r["station"] == "$station")""" else ""
        val fluxQuery = """
            from(bucket: "${influxDBInitializer.bucketName}")
              |> range(start: 0)
              |> filter(fn: (r) => r["_measurement"] == "rainfall")
              $stationFilter
              |> pivot(rowKey:["_time"], columnKey: ["_field"], valueColumn: "_value")
              |> sort(columns: ["_time"], desc: true)
              |> limit(n: $samplePoints)
        """.trimIndent()
        return executeQuery(fluxQuery)
    }

    private fun executeQuery(fluxQuery: String): List<RainfallData> {
        val tables = influxDBInitializer.queryApi.query(fluxQuery)
        val dataList = mutableListOf<RainfallData>()
        for (table in tables) {
            for (record in table.records) {
                dataList.add(mapToRainfallData(record))
            }
        }
        return dataList
    }

    private fun mapToRainfallData(record: FluxRecord): RainfallData {
        return RainfallData(
            timestamp = record.time?.toEpochMilli() ?: 0,
            rainfallIncrement = (record.getValueByKey("rainfall_increment") as? Double) ?: 0.0,
            totalRainfall = (record.getValueByKey("total_rainfall") as? Double) ?: 0.0
        )
    }
}