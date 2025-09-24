package com.sharingplate.mlservice.infrastructure.inbound.web

import com.sharingplate.mlservice.infrastructure.outbound.ml.MLModelService
import com.sharingplate.mlservice.infrastructure.outbound.ml.payload.MLPredictionRequest
import com.sharingplate.mlservice.infrastructure.outbound.ml.payload.MLPredictionResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/monitoring")
class MineMonitoringController(
    private val mlModelService: MLModelService
) {
    private val logger = LoggerFactory.getLogger(MineMonitoringController::class.java)

    @PostMapping("/predict-risk")
    fun predictRisk(@RequestBody data: MLPredictionRequest): ResponseEntity<MLPredictionResponse> {
        return try {
            val riskLevel = mlModelService.predictRisk(data)
            logger.info("Risk prediction completed for station: ${data.stationId}, risk: $riskLevel")
            ResponseEntity.ok(riskLevel)
        } catch (e: Exception) {
            logger.error("Error predicting risk for station ${data.stationId}", e)
            ResponseEntity.internalServerError().build()
        }
    }
}