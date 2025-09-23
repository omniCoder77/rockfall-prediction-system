package com.sharingplate.sensorservice.infrastructure.outbound.influxdb

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import com.influxdb.query.FluxRecord
import com.sharingplate.sensorservice.domain.model.StrainData
import com.sharingplate.sensorservice.domain.port.driven.StrainDataRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class StrainDataRepositoryImpl(private val influxDBInitializer: InfluxDBInitializer) : StrainDataRepository {
    override fun save(
        station: String,
        data: StrainData
    ) {
        val point = Point("strain")
            .addTag("station", station)
            .addField("frequency", data.frequency)
            .addField("temperature", data.temperature)
            .addField("strain_value", data.strainValue)
            .time(Instant.now(), WritePrecision.NS)
        influxDBInitializer.influxDbWriter.writePoint(point)
    }

    override fun readByStation(
        start: String,
        end: String,
        station: String?,
        windowLengthInSeconds: Int
    ): List<StrainData> {
        val stationFilter = if (station != null) """|> filter(fn: (r) => r["station"] == "$station")""" else ""
        val fluxQuery = """
            from(bucket: "${influxDBInitializer.bucketName}")
              |> range(start: $start, stop: $end)
              |> filter(fn: (r) => r["_measurement"] == "strain")
              $stationFilter
              |> aggregateWindow(every: ${windowLengthInSeconds}s, fn: mean, createEmpty: false)
              |> pivot(rowKey:["_time"], columnKey: ["_field"], valueColumn: "_value")
        """.trimIndent()
        return executeQuery(fluxQuery)
    }

    override fun readByPoints(samplePoints: Int, station: String?): List<StrainData> {
        val stationFilter = if (station != null) """|> filter(fn: (r) => r["station"] == "$station")""" else ""
        val fluxQuery = """
            from(bucket: "${influxDBInitializer.bucketName}")
              |> range(start: 0)
              |> filter(fn: (r) => r["_measurement"] == "strain")
              $stationFilter
              |> pivot(rowKey:["_time"], columnKey: ["_field"], valueColumn: "_value")
              |> sort(columns: ["_time"], desc: true)
              |> limit(n: $samplePoints)
        """.trimIndent()
        return executeQuery(fluxQuery)
    }

    private fun executeQuery(fluxQuery: String): List<StrainData> {
        val tables = influxDBInitializer.queryApi.query(fluxQuery)
        val dataList = mutableListOf<StrainData>()
        for (table in tables) {
            for (record in table.records) {
                dataList.add(mapToStrainData(record))
            }
        }
        return dataList
    }

    private fun mapToStrainData(record: FluxRecord): StrainData {
        return StrainData(
            timestamp = record.time?.toEpochMilli() ?: 0,
            strainValue = (record.getValueByKey("strain_value") as? Double) ?: 0.0,
            temperature = (record.getValueByKey("temperature") as? Double) ?: 0.0,
            frequency = (record.getValueByKey("frequency") as? Double) ?: 0.0
        )
    }
}