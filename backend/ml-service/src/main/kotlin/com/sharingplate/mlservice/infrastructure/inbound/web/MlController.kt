package com.sharingplate.mlservice.infrastructure.inbound.web

import com.sharingplate.mlservice.application.MlPredictionService
import com.sharingplate.mlservice.infrastructure.outbound.mlapi.ModelHealth
import com.sharingplate.mlservice.infrastructure.outbound.mlapi.ModelInfo
import com.sharingplate.mlservice.infrastructure.outbound.mlapi.PredictionRequest
import com.sharingplate.mlservice.infrastructure.outbound.mlapi.PredictionResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/ml")
class MlController(private val mlPredictionService: MlPredictionService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/predict")
    fun predict(@RequestBody request: PredictionRequest): Mono<ResponseEntity<PredictionResponse>> {
        logger.info("API: Received prediction request for station: {}", request.stationId)
        return mlPredictionService.predictRockfall(request)
            .map { prediction -> ResponseEntity.ok(prediction) }
            .onErrorResume { e ->
                logger.error("API error during prediction for station {}: {}", request.stationId, e.message, e)
                Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
            }
    }

    @GetMapping("/health")
    fun getModelHealth(): Mono<ResponseEntity<ModelHealth>> {
        logger.info("API: Received request for ML model health.")
        return mlPredictionService.getModelHealthStatus()
            .map { health -> ResponseEntity.ok(health) }
            .onErrorResume { e ->
                logger.error("API error checking ML model health: {}", e.message, e)
                Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                    ModelHealth(status = "DOWN", message = "Could not connect to ML model service.", timestamp = "Current time")))
            }
    }

    @GetMapping("/info")
    fun getModelInfo(): Mono<ResponseEntity<ModelInfo>> {
        logger.info("API: Received request for ML model info.")
        return mlPredictionService.getModelDetails()
            .map { info -> ResponseEntity.ok(info) }
            .onErrorResume { e ->
                logger.error("API error fetching ML model info: {}", e.message, e)
                Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build())
            }
    }
}