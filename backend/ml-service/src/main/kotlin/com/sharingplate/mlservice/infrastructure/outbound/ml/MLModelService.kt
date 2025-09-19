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

    fun predictRisk(data: MLPredictionRequest): Double {
        return try {
            val request = convertToMLRequest(data)
            val url = "$mlModelBaseUrl/predict"

            logger.debug("Calling ML model at: $url")
            val response = restTemplate.postForObject(
                url, request, MLPredictionResponse::class.java
            )

            response?.risk_level ?: throw RuntimeException("Null response from ML model")
        } catch (e: Exception) {
            logger.error("Failed to call ML model for station: ${data.stationId}", e)
            throw RuntimeException("ML model prediction failed", e)
        }
    }

    private fun convertToMLRequest(data: MLPredictionRequest): MLPredictionRequest {
        return MLPredictionRequest(
            timestamp = data.timestamp,
            stationId = data.stationId,
            zone = data.zone,
            lat = data.lat,
            lon = data.lon,
            elevationM = data.elevationM,
            rainfallMm = data.rainfallMm,
            dispIncMm = data.dispIncMm,
            strainIncMicro = data.strainIncMicro,
            poreKpa = data.poreKpa,
            vibration = data.vibration,
            blastFlag = data.blastFlag,
            sensorStatus = data.sensorStatus,
            cumulativeDispMm = data.cumulativeDispMm,
            dispRate6h = data.dispRate6h,
            strain6h = data.strain6h,
            pore6h = data.pore6h,
            vib6h = data.vib6h,
            riskScoreRaw = data.riskScoreRaw,
            riskScoreNorm = data.riskScoreNorm,
            riskLabel = data.riskLabel,
            eventProb = data.eventProb,
            rockfallEvent = data.rockfallEvent
        )
    }
}