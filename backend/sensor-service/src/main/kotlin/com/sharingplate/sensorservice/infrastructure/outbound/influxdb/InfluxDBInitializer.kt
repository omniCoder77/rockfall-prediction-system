package com.sharingplate.sensorservice.infrastructure.outbound.influxdb

import com.influxdb.client.InfluxDBClient
import com.influxdb.client.InfluxDBClientFactory
import com.influxdb.client.WriteOptions
import io.reactivex.rxjava3.core.BackpressureOverflowStrategy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

@Component
class InfluxDBInitializer(
    @Value("\${influxdb.url}") private val databaseURL: String,
    @Value("\${influxdb.token}") private val token: String,
    @Value("\${influxdb.bucket}") val bucketName: String,
    @Value("\${influxdb.org}") private val organization: String
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val influxDBClient: InfluxDBClient =
        InfluxDBClientFactory.create(databaseURL, token.toCharArray(), organization, bucketName)
    val queryApi = influxDBClient.queryApi
    val influxDbWriter = influxDBClient.makeWriteApi(
        WriteOptions.builder().backpressureStrategy(
            BackpressureOverflowStrategy.DROP_OLDEST
        ).batchSize(100).build()
    )

    init {
        val ping = influxDBClient.ping()
        if (ping) {
            logger.info("Successfully connected to InfluxDB 2.x at $databaseURL")
        } else {
            logger.error("Failed to connect to InfluxDB 2.x at $databaseURL")
            throw RuntimeException("Failed to connect to InfluxDB 2.x at $databaseURL")
            influxDBClient.close()
        }
    }


    fun deleteAllDataInBucket() {
        try {
            val now = OffsetDateTime.now()
            influxDBClient.deleteApi.delete(
                OffsetDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault()),
                now,
                "",
                bucketName,
                organization
            )
            logger.warn("All data deleted from bucket '$bucketName'")
        } catch (e: Exception) {
            logger.error("Failed to delete data from bucket '$bucketName': ${e.message}", e)
        }
    }

    @EventListener(ContextClosedEvent::class)
    fun destroy() {
        influxDbWriter.flush()
        influxDbWriter.close()
        influxDBClient.close()
        logger.info("InfluxDB client closed")
    }
}