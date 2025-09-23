package com.sharingplate.sensorservice.infrastructure.inbound.grpc

import com.sharingplate.sensorservice.infrastructure.inbound.grpc.dto.PredictionRequest
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class AlertService(
    @Value("\${ml-service.name}") private val mlServiceName: String,
    @Value("\${alert.threshold.fluctuation.default:0.1}") private val defaultFluctuationThreshold: Double // Default 10%
) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private lateinit var channel: ManagedChannel
    private lateinit var asyncStub: AlertServiceGrpc.AlertServiceStub

    @PostConstruct
    fun init() {
        channel = ManagedChannelBuilder.forTarget(mlServiceName).usePlaintext().build()
        asyncStub = AlertServiceGrpc.newStub(channel)
        logger.info("gRPC AlertService initialized, connecting to ML service at: $mlServiceName")
    }

    @PreDestroy
    fun shutdown() {
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
            logger.info("gRPC channel to ML service shut down.")
        } catch (e: InterruptedException) {
            logger.warn("gRPC channel shutdown interrupted: ${e.message}")
        }
    }


    fun sensorDown(sensorId: String, stationId: String) {
        val request = SensorDownRequest.newBuilder().setSensorClientId(sensorId).setStationId(stationId).build()
        asyncStub.sensorDown(request, object : StreamObserver<SensorDownResponse> {
            override fun onNext(p0: SensorDownResponse?) {
                logger.info("Alert: Sensor $sensorId at station $stationId reported as down.")
            }

            override fun onError(p0: Throwable?) {
                logger.error("Error occurred while reporting sensor down alert for $sensorId: ${p0?.message}", p0)
            }

            override fun onCompleted() {
                //logger.debug("Sensor down alert for $sensorId completed.")
            }
        })
    }

    fun valueFluctuated(sensorId: String, stationId: String, previousValue: Double, currentValue: Double, measurementType: String) {
        val request = ValueFluctuateRequest.newBuilder()
            .setSensorClientId(sensorId)
            .setStationId(stationId)
            .setPreviousValue(previousValue)
            .setCurrentValue(currentValue)
            .build()
        asyncStub.valueFluctuate(
            request, object : StreamObserver<ValueFluctuateResponse> {
                override fun onNext(p0: ValueFluctuateResponse?) {
                    logger.info("Alert: Value fluctuation for $measurementType sensor $sensorId at station $stationId (prev: $previousValue, curr: $currentValue)")
                }

                override fun onError(p0: Throwable?) {
                    logger.error("Error occurred while reporting value fluctuation for $sensorId: ${p0?.message}", p0)
                }

                override fun onCompleted() {
                    //logger.debug("Value fluctuation alert for $sensorId completed.")
                }
            })
    }

    fun sendBulkData(predictionRequest: List<PredictionRequest>) {
        if (predictionRequest.isEmpty()) {
            logger.debug("No bulk data to send for prediction.")
            return
        }

        val builder = BulkDataRequest.newBuilder()
        for (req in predictionRequest) {
            val mlPredictionRequest =
                MLPredictionRequest.newBuilder().setStationId(req.stationId).setLon(req.lon).setLat(req.lat)
                    .setVibration(req.vibration).setDispIncMm(req.dispIncMm).setElevationM(req.elevationM)
                    .setPoreKpa(req.poreKpa).setStrainIncMicro(req.strainIncMicro).setZone(req.zone)
                    .setTimestamp(req.timestamp).setRainfallMm(req.rainfallMm) // Add rainfall
                    .build()
            builder.addMlPredictRequest(mlPredictionRequest)
        }
        asyncStub.bulkData(builder.build(), object : StreamObserver<BulkDataResponse> {
            override fun onNext(p0: BulkDataResponse?) {
                logger.info("Bulk data sent successfully. ML service processed ${p0?: 0} records.")
            }

            override fun onError(p0: Throwable?) {
                logger.error("Error occurred while sending bulk data for prediction: ${p0?.message}", p0)
            }

            override fun onCompleted() {
                //logger.debug("Bulk data send completed.")
            }
        })
    }
}