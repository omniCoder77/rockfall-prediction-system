package com.sharingplate.mlservice.infrastructure.outbound.ml

import com.sharingplate.mlservice.infrastructure.outbound.ml.payload.MLPredictionRequest
import com.sharingplate.mlservice.infrastructure.outbound.ml.payload.MLPredictionResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class MLModelService(
    private val restTemplate: RestTemplate
) {
    private val logger = LoggerFactory.getLogger(MLModelService::class.java)

    @Value("\${ml.model.url:http://localhost:5000}")
    private lateinit var mlModelBaseUrl: String

    fun predictRisk(data: MLPredictionRequest): MLPredictionResponse {
        return try {
            val url = "$mlModelBaseUrl/predict"

            logger.debug("Calling ML model at: $url")
            val response = restTemplate.postForObject(
                url, data, MLPredictionResponse::class.java
            )

            response ?: throw RuntimeException("Null response from ML model")
        } catch (e: Exception) {
            logger.error("Failed to call ML model for station: ${data.stationId}", e)
            throw RuntimeException("ML model prediction failed", e)
        }
    }
}