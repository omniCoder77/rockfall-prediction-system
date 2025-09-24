package com.sharingplate.sensorservice.infrastructure.outbound.influxdb

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import com.influxdb.query.FluxRecord
import com.sharingplate.sensorservice.domain.model.DisplacementData
import com.sharingplate.sensorservice.domain.port.driven.DisplacementDataRepository
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class DisplacementDataRepositoryImpl(private val influxDBInitializer: InfluxDBInitializer) :
    DisplacementDataRepository {
    override fun save(data: DisplacementData, station: String) {
        val point = Point.measurement("displacement").addTag("station", station).addField("tilt_x", data.tiltX)
            .addField("tilt_y", data.tiltY).addField("temperature", data.temperature)
            .time(Instant.now(), WritePrecision.NS)
        influxDBInitializer.influxDbWriter.writePoint(point)
    }

    override fun readByStation(
        start: String, end: String, station: String?, windowLengthInSeconds: Int
    ): List<DisplacementData> {
        val stationFilter = if (station != null) """|> filter(fn: (r) => r["station"] == "$station")""" else ""
        val fluxQuery = """
            from(bucket: "${influxDBInitializer.bucketName}")
              |> range(start: $start, stop: $end)
              |> filter(fn: (r) => r["_measurement"] == "displacement")
              $stationFilter
              |> aggregateWindow(every: ${windowLengthInSeconds}s, fn: mean, createEmpty: false)
              |> pivot(rowKey:["_time"], columnKey: ["_field"], valueColumn: "_value")
        """.trimIndent()

        return executeQuery(fluxQuery)
    }

    override fun readByPoints(samplePoints: Int, station: String?): List<DisplacementData> {
        val stationFilter = if (station != null) """|> filter(fn: (r) => r["station"] == "$station")""" else ""
        val fluxQuery = """
            from(bucket: "${influxDBInitializer.bucketName}")
              |> range(start: 0)
              |> filter(fn: (r) => r["_measurement"] == "displacement")
              $stationFilter
              |> pivot(rowKey:["_time"], columnKey: ["_field"], valueColumn: "_value")
              |> sort(columns: ["_time"], desc: true)
              |> limit(n: $samplePoints)
        """.trimIndent()

        return executeQuery(fluxQuery)
    }

    private fun executeQuery(fluxQuery: String): List<DisplacementData> {
        val tables = influxDBInitializer.queryApi.query(fluxQuery)
        val dataList = mutableListOf<DisplacementData>()
        for (table in tables) {
            for (record in table.records) {
                dataList.add(mapToDisplacementData(record))
            }
        }
        return dataList
    }

    private fun mapToDisplacementData(record: FluxRecord): DisplacementData {
        return DisplacementData(
            tiltX = (record.getValueByKey("tilt_x") as? Double) ?: 0.0,
            tiltY = (record.getValueByKey("tilt_y") as? Double) ?: 0.0,
            temperature = (record.getValueByKey("temperature") as? Double) ?: 0.0
        )
    }
}