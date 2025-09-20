package com.sharingplate.mlservice.infrastructure.outbound.mlapi

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class MlApiClient(
    private val mlModelWebClient: WebClient,
    @Value("\${ml.model.predict-endpoint}") private val predictEndpoint: String,
    @Value("\${ml.model.health-endpoint}") private val healthEndpoint: String,
    @Value("\${ml.model.info-endpoint}") private val infoEndpoint: String
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun getPrediction(request: PredictionRequest): Mono<PredictionResponse> {
        logger.info("Sending prediction request to ML model for station: {}", request.stationId)
        return mlModelWebClient.post()
            .uri(predictEndpoint)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(PredictionResponse::class.java)
            .doOnSuccess { response -> logger.debug("Received prediction response for station {}: {}", request.stationId, response) }
            .doOnError { e -> logger.error("Error calling ML predict endpoint for station {}: {}", request.stationId, e.message, e) }
    }

    fun getModelHealth(): Mono<ModelHealth> {
        logger.debug("Checking ML model health.")
        return mlModelWebClient.get()
            .uri(healthEndpoint)
            .retrieve()
            .bodyToMono(ModelHealth::class.java)
            .doOnSuccess { health -> logger.debug("ML model health status: {}", health.status) }
            .doOnError { e -> logger.error("Error checking ML model health: {}", e.message, e) }
    }

    fun getModelInfo(): Mono<ModelInfo> {
        logger.debug("Fetching ML model information.")
        return mlModelWebClient.get()
            .uri(infoEndpoint)
            .retrieve()
            .bodyToMono(ModelInfo::class.java)
            .doOnSuccess { info -> logger.debug("ML model info: {}", info.model_exists) }
            .doOnError { e -> logger.error("Error fetching ML model information: {}", e.message, e) }
    }
}