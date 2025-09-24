package com.sharingplate.mlservice.infrastructure.inbound.kafka

import com.google.gson.Gson
import com.sharingplate.mlservice.infrastructure.inbound.kafka.dto.SensorFluctuationEvent
import com.sharingplate.mlservice.infrastructure.outbound.ml.MLModelService
import com.sharingplate.mlservice.infrastructure.outbound.ml.payload.MLPredictionRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class EventListener(
    private val mLModelService: MLModelService,
    @Value("\${risk.threshold}") private val riskThreshold: Double,
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    @KafkaListener(topics = ["sensor-fluctuation-events"], groupId = "ml-service")
    fun kafkaListener(message: String) {
        val data = Gson().fromJson(message, SensorFluctuationEvent::class.java)
        val res = mLModelService.predictRisk(MLPredictionRequest(data))
        if (res.riskLevel > riskThreshold) {
            val alertMessage = Gson().toJson(res)
            kafkaTemplate.send("alert-message", alertMessage)
        }
    }
}