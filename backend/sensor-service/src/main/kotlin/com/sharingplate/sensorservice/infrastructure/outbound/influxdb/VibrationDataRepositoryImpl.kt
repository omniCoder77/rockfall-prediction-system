package com.sharingplate.sensorservice.infrastructure.outbound.influxdb

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import com.influxdb.query.FluxRecord
import com.sharingplate.sensorservice.domain.model.VibrationData
import com.sharingplate.sensorservice.domain.port.driven.VibrationDataRepository
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class VibrationDataRepositoryImpl(private val influxDBInitializer: InfluxDBInitializer) : VibrationDataRepository {
    override fun save(
        station: String,
        data: VibrationData
    ) {
        val point = Point("vibration")
            .addTag("station", station)
            .addField("acc_x", data.accelX)
            .addField("acc_y", data.accelY)
            .addField("acc_z", data.accelZ)
            .addField("magnitude", data.magnitude)
            .time(Instant.now(), WritePrecision.NS)
        influxDBInitializer.influxDbWriter.writePoint(point)
    }

    override fun readByStation(
        start: String,
        end: String,
        station: String?,
        windowLengthInSeconds: Int
    ): List<VibrationData> {
        val stationFilter = if (station != null) """|> filter(fn: (r) => r["station"] == "$station")""" else ""
        val fluxQuery = """
            from(bucket: "${influxDBInitializer.bucketName}")
              |> range(start: $start, stop: $end)
              |> filter(fn: (r) => r["_measurement"] == "vibration")
              $stationFilter
              |> aggregateWindow(every: ${windowLengthInSeconds}s, fn: mean, createEmpty: false)
              |> pivot(rowKey:["_time"], columnKey: ["_field"], valueColumn: "_value")
        """.trimIndent()
        return executeQuery(fluxQuery)
    }

    override fun readByPoints(samplePoints: Int, station: String?): List<VibrationData> {
        val stationFilter = if (station != null) """|> filter(fn: (r) => r["station"] == "$station")""" else ""
        val fluxQuery = """
            from(bucket: "${influxDBInitializer.bucketName}")
              |> range(start: 0)
              |> filter(fn: (r) => r["_measurement"] == "vibration")
              $stationFilter
              |> pivot(rowKey:["_time"], columnKey: ["_field"], valueColumn: "_value")
              |> sort(columns: ["_time"], desc: true)
              |> limit(n: $samplePoints)
        """.trimIndent()
        return executeQuery(fluxQuery)
    }

    private fun executeQuery(fluxQuery: String): List<VibrationData> {
        val tables = influxDBInitializer.queryApi.query(fluxQuery)
        val dataList = mutableListOf<VibrationData>()
        for (table in tables) {
            for (record in table.records) {
                dataList.add(mapToVibrationData(record))
            }
        }
        return dataList
    }

    private fun mapToVibrationData(record: FluxRecord): VibrationData {
        return VibrationData(
            timestamp = record.time?.toEpochMilli() ?: 0,
            accelX = (record.getValueByKey("acc_x") as? Double) ?: 0.0,
            accelY = (record.getValueByKey("acc_y") as? Double) ?: 0.0,
            accelZ = (record.getValueByKey("acc_z") as? Double) ?: 0.0,
            magnitude = (record.getValueByKey("magnitude") as? Double) ?: 0.0
        )
    }
}