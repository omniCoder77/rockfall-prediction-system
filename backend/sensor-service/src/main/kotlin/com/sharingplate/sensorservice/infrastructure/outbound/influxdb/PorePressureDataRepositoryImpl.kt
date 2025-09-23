package com.sharingplate.sensorservice.infrastructure.outbound.influxdb

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import com.influxdb.query.FluxRecord
import com.sharingplate.sensorservice.domain.model.PorePressureData
import com.sharingplate.sensorservice.domain.port.driven.PorePressureDataRepository
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class PorePressureDataRepositoryImpl(private val influxDBInitializer: InfluxDBInitializer) : PorePressureDataRepository {
    override fun save(station: String, data: PorePressureData) {
        val point = Point("pore_pressure")
            .addTag("station", station)
            .addField("temperature", data.temperature)
            .addField("pressure", data.pressure)
            .addField("frequency", data.frequency)
            .time(Instant.now(), WritePrecision.NS)
        influxDBInitializer.influxDbWriter.writePoint(point)
    }

    override fun readByStation(
        start: String,
        end: String,
        station: String?,
        windowLengthInSeconds: Int
    ): List<PorePressureData> {
        val stationFilter = if (station != null) """|> filter(fn: (r) => r["station"] == "$station")""" else ""
        val fluxQuery = """
            from(bucket: "${influxDBInitializer.bucketName}")
              |> range(start: $start, stop: $end)
              |> filter(fn: (r) => r["_measurement"] == "pore_pressure")
              $stationFilter
              |> aggregateWindow(every: ${windowLengthInSeconds}s, fn: mean, createEmpty: false)
              |> pivot(rowKey:["_time"], columnKey: ["_field"], valueColumn: "_value")
        """.trimIndent()
        return executeQuery(fluxQuery)
    }

    override fun readByPoints(samplePoints: Int, station: String?): List<PorePressureData> {
        val stationFilter = if (station != null) """|> filter(fn: (r) => r["station"] == "$station")""" else ""
        val fluxQuery = """
            from(bucket: "${influxDBInitializer.bucketName}")
              |> range(start: 0)
              |> filter(fn: (r) => r["_measurement"] == "pore_pressure")
              $stationFilter
              |> pivot(rowKey:["_time"], columnKey: ["_field"], valueColumn: "_value")
              |> sort(columns: ["_time"], desc: true)
              |> limit(n: $samplePoints)
        """.trimIndent()
        return executeQuery(fluxQuery)
    }

    private fun executeQuery(fluxQuery: String): List<PorePressureData> {
        val tables = influxDBInitializer.queryApi.query(fluxQuery)
        val dataList = mutableListOf<PorePressureData>()
        for (table in tables) {
            for (record in table.records) {
                dataList.add(mapToPorePressureData(record))
            }
        }
        return dataList
    }

    private fun mapToPorePressureData(record: FluxRecord): PorePressureData {
        return PorePressureData(
            pressure = (record.getValueByKey("pressure") as? Double) ?: 0.0,
            temperature = (record.getValueByKey("temperature") as? Double) ?: 0.0,
            frequency = (record.getValueByKey("frequency") as? Double) ?: 0.0
        )
    }
}