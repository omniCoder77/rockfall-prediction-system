package com.sharingplate.apigateway.infrastructure.persistence.influxdb

import com.influxdb.client.InfluxDBClient
import com.influxdb.client.InfluxDBClientFactory
import com.influxdb.client.WriteApiBlocking
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import com.sharingplate.apigateway.inbound.mqtt.dto.*
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.collections.isNotEmpty

@Component
class InfluxDBInitializer(
    @Value("\${influxdb.url}") private val databaseURL: String,
    @Value("\${influxdb.token}") private val token: String,
    @Value("\${influxdb.bucket}") private val bucketName: String,
    @Value("\${influxdb.org}") private val organization: String
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val influxDBClient: InfluxDBClient = InfluxDBClientFactory.create(databaseURL, token.toCharArray(), organization, bucketName)
    private val writeApi: WriteApiBlocking = influxDBClient.writeApiBlocking

    private val displacementPoints = ConcurrentLinkedQueue<Point>()
    private val strainPoints = ConcurrentLinkedQueue<Point>()
    private val pressurePoints = ConcurrentLinkedQueue<Point>()
    private val rainfallPoints = ConcurrentLinkedQueue<Point>()
    private val vibrationPoints = ConcurrentLinkedQueue<Point>()
    private val temperaturePoints = ConcurrentLinkedQueue<Point>()
    private val dronePoints = ConcurrentLinkedQueue<Point>()

    init {
        val ping = influxDBClient.ping()
        if (ping) {
            logger.info("Successfully connected to InfluxDB 2.x at $databaseURL")
        } else {
            logger.error("Failed to connect to InfluxDB 2.x at $databaseURL")
        }
    }

    fun addDisplacementData(data: DisplacementData) {
        val point = Point.measurement("displacement")
            .addTag("station_id", data.stationId)
            .addField("tilt_x", data.tiltX)
            .addField("tilt_y", data.tiltY)
            .addField("temperature", data.temperature)
            .time(data.timestamp, WritePrecision.MS)
        displacementPoints.offer(point)
    }

    fun addStrainData(data: StrainData) {
        val point = Point.measurement("strain")
            .addTag("station_id", data.stationId)
            .addField("strain_value", data.strainValue)
            .addField("temperature", data.temperature)
            .addField("frequency", data.frequency)
            .time(data.timestamp, WritePrecision.MS)
        strainPoints.offer(point)
    }

    fun addPressureData(data: PorePressureData) {
        val point = Point.measurement("pore_pressure")
            .addTag("station_id", data.stationId)
            .addField("pressure", data.pressure)
            .addField("temperature", data.temperature)
            .addField("frequency", data.frequency)
            .time(data.timestamp, WritePrecision.MS)
        pressurePoints.offer(point)
    }

    fun addRainfallData(data: RainfallData) {
        val point = Point.measurement("rainfall")
            .addTag("station_id", data.stationId) // Changed to tag - more efficient
            .addField("rainfall_increment", data.rainfallIncrement)
            .addField("total_rainfall", data.totalRainfall)
            .time(data.timestamp, WritePrecision.MS)
        rainfallPoints.offer(point)
    }

    fun addVibrationData(data: VibrationData) {
        val point = Point.measurement("vibration")
            .addTag("station_id", data.stationId)
            .addField("accel_x", data.accelX)
            .addField("accel_y", data.accelY)
            .addField("accel_z", data.accelZ)
            .addField("magnitude", data.magnitude)
            .time(data.timestamp, WritePrecision.MS)
        vibrationPoints.offer(point)
    }

    fun addTemperatureData(data: TemperatureData) {
        val point = Point.measurement("temperature")
            .addTag("station_id", data.stationId)
            .addField("temperature", data.temperature)
            .addField("humidity", data.humidity)
            .time(data.timestamp, WritePrecision.MS)
        temperaturePoints.offer(point)
    }

    fun addDroneData(data: DroneImageData) {
        val point = Point.measurement("drone_image")
            .addTag("drone_id", data.droneId)
            .addField("gps_latitude", data.gpsLatitude)
            .addField("gps_longitude", data.gpsLongitude)
            .addField("altitude", data.altitude)
            .addField("image_size", data.imageData.size.toLong())
            .time(data.timestamp, WritePrecision.MS)
        dronePoints.offer(point)
    }

    @Scheduled(fixedRate = 3000)
    fun writeBatchData() {
        try {
            val allPoints = drainAllQueues()

            if (allPoints.isNotEmpty()) {
                writeApi.writePoints(allPoints)
                logger.debug("Successfully wrote batch of ${allPoints.size} points to InfluxDB")
            }
        } catch (e: Exception) {
            logger.error("Error writing batch to InfluxDB: ${e.message}", e)
        }
    }

    private fun drainAllQueues(): List<Point> {
        val allPoints = mutableListOf<Point>()

        allPoints.addAll(drainQueue(displacementPoints))
        allPoints.addAll(drainQueue(strainPoints))
        allPoints.addAll(drainQueue(pressurePoints))
        allPoints.addAll(drainQueue(rainfallPoints))
        allPoints.addAll(drainQueue(vibrationPoints))
        allPoints.addAll(drainQueue(temperaturePoints))
        allPoints.addAll(drainQueue(dronePoints))

        return allPoints
    }

    private fun drainQueue(queue: ConcurrentLinkedQueue<Point>): List<Point> {
        val points = mutableListOf<Point>()
        while (true) {
            val point = queue.poll() ?: break
            points.add(point)
        }
        return points
    }

    @PreDestroy
    fun closeConnection() {
        try {
            val remainingPoints = drainAllQueues()
            if (remainingPoints.isNotEmpty()) {
                writeApi.writePoints(remainingPoints)
                logger.info("Flushed final ${remainingPoints.size} points before shutdown")
            }

            influxDBClient.close()
            logger.info("InfluxDB 2.x connection closed successfully")
        } catch (e: Exception) {
            logger.error("Error during InfluxDB shutdown: ${e.message}", e)
        }
    }
}