package com.sharingplate.sensorservice.infrastructure.outbound.influxdb

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import com.influxdb.query.FluxRecord
import com.sharingplate.sensorservice.domain.model.DroneImageData
import com.sharingplate.sensorservice.domain.port.driven.DroneImageDataRepository
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class DroneImageDataRepositoryImpl(private val influxDBInitializer: InfluxDBInitializer) : DroneImageDataRepository {
    override fun save(station: String, data: DroneImageData) {
        val point = Point("drone_image_data")
            .addTag("station", station)
            .addField("image_data", String(data.imageData))
            .addField("altitude", data.altitude)
            .addField("lat", data.lat)
            .addField("long", data.long)
            .time(Instant.now(), WritePrecision.NS)
        influxDBInitializer.influxDbWriter.writePoint(point)
    }

    override fun readByStation(
        start: String,
        end: String,
        station: String?,
        windowLengthInSeconds: Int
    ): List<DroneImageData> {
        val stationFilter = if (station != null) """|> filter(fn: (r) => r["station"] == "$station")""" else ""
        val fluxQuery = """
            from(bucket: "${influxDBInitializer.bucketName}")
              |> range(start: $start, stop: $end)
              |> filter(fn: (r) => r["_measurement"] == "drone_image_data")
              $stationFilter
              |> aggregateWindow(every: ${windowLengthInSeconds}s, fn: last, createEmpty: false)
              |> pivot(rowKey:["_time"], columnKey: ["_field"], valueColumn: "_value")
        """.trimIndent()
        return executeQuery(fluxQuery)
    }

    override fun readByPoints(samplePoints: Int, station: String?): List<DroneImageData> {
        val stationFilter = if (station != null) """|> filter(fn: (r) => r["station"] == "$station")""" else ""
        val fluxQuery = """
            from(bucket: "${influxDBInitializer.bucketName}")
              |> range(start: 0)
              |> filter(fn: (r) => r["_measurement"] == "drone_image_data")
              $stationFilter
              |> pivot(rowKey:["_time"], columnKey: ["_field"], valueColumn: "_value")
              |> sort(columns: ["_time"], desc: true)
              |> limit(n: $samplePoints)
        """.trimIndent()
        return executeQuery(fluxQuery)
    }

    private fun executeQuery(fluxQuery: String): List<DroneImageData> {
        val tables = influxDBInitializer.queryApi.query(fluxQuery)
        val dataList = mutableListOf<DroneImageData>()
        for (table in tables) {
            for (record in table.records) {
                dataList.add(mapToDroneImageData(record))
            }
        }
        return dataList
    }

    private fun mapToDroneImageData(record: FluxRecord): DroneImageData {
        return DroneImageData(
            imageData = (record.getValueByKey("image_data") as? String)?.toByteArray() ?: byteArrayOf(),
            lat = (record.getValueByKey("lat") as? Double) ?: 0.0,
            long = (record.getValueByKey("long") as? Double) ?: 0.0,
            altitude = (record.getValueByKey("altitude") as? Double) ?: 0.0
        )
    }
}