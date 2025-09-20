package com.sharingplate.mlservice.application

import com.sharingplate.mlservice.infrastructure.outbound.mlapi.MlApiClient
import com.sharingplate.mlservice.infrastructure.outbound.mlapi.ModelHealth
import com.sharingplate.mlservice.infrastructure.outbound.mlapi.ModelInfo
import com.sharingplate.mlservice.infrastructure.outbound.mlapi.PredictionRequest
import com.sharingplate.mlservice.infrastructure.outbound.mlapi.PredictionResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class MlPredictionService(private val mlApiClient: MlApiClient) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun predictRockfall(request: PredictionRequest): Mono<PredictionResponse> {
        logger.info("Service: Requesting rockfall prediction for station: {}", request.stationId)
        return mlApiClient.getPrediction(request)
            .doOnError { e -> logger.error("Service error during rockfall prediction for station {}: {}", request.stationId, e.message, e) }
    }

    fun getModelHealthStatus(): Mono<ModelHealth> {
        logger.info("Service: Checking ML model health status.")
        return mlApiClient.getModelHealth()
            .doOnError { e -> logger.error("Service error checking ML model health: {}", e.message, e) }
    }

    fun getModelDetails(): Mono<ModelInfo> {
        logger.info("Service: Fetching ML model details.")
        return mlApiClient.getModelInfo()
            .doOnError { e -> logger.error("Service error fetching ML model details: {}", e.message, e) }
    }
}