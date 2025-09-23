package com.sharingplate.sensorservice.infrastructure.inbound.web

import com.sharingplate.sensorservice.domain.contants.SensorType
import com.sharingplate.sensorservice.infrastructure.inbound.mqtt.SensorService
import com.sharingplate.sensorservice.infrastructure.inbound.web.dto.request.AddSensorRequest
import com.sharingplate.sensorservice.infrastructure.inbound.web.dto.request.DeleteSensorRequest
import com.sharingplate.sensorservice.infrastructure.inbound.web.dto.response.SensorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/sensors")
class SensorController(
    private val sensorService: SensorService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping
    fun addSensor(@RequestBody addSensorRequest: AddSensorRequest): ResponseEntity<String> {
        return try {
            sensorService.addSensor(addSensorRequest.stationId, addSensorRequest.sensorType)
            ResponseEntity.ok().build()
        } catch (e: IllegalArgumentException) {
            logger.error(e.message, e)
            ResponseEntity.badRequest()
                .body("${addSensorRequest.sensorType.name} sensor is already present at station ${addSensorRequest.stationId}")
        } catch (e: Exception) {
            logger.error(e.message, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @PatchMapping
    fun updateSensor(@RequestBody addSensorRequest: AddSensorRequest): ResponseEntity<String> {
        return try {
            sensorService.updateSensor(addSensorRequest.stationId, addSensorRequest.sensorType)
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            logger.error(e.message, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @DeleteMapping
    fun removeSensor(@RequestBody deleteSensorRequest: DeleteSensorRequest): ResponseEntity<String> {
        return try {
            sensorService.removeSensor(deleteSensorRequest.stationId, deleteSensorRequest.sensorType)
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            logger.error(e.message, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/active-sensors")
    fun getActiveSensors(): ResponseEntity<List<SensorResponse>> {
        return try {
            val activeSensors = sensorService.getActiveSensors().map { SensorResponse(it.stationId, SensorType.valueOf(it.sensorType)) }
            ResponseEntity.ok(activeSensors)
        } catch (e: Exception) {
            logger.error(e.message, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/unactive-sensors")
    fun getUnactiveSensors(): ResponseEntity<List<SensorResponse>> {
        return try {
            val activeSensors = sensorService.getInactiveSensors().map { SensorResponse(it.stationId, SensorType.valueOf(it.sensorType)) }
            ResponseEntity.ok(activeSensors)
        } catch (e: Exception) {
            logger.error(e.message, e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/sensors")
    fun getSensors(): ResponseEntity<List<SensorResponse>> {
        return try {
            val activeSensors = sensorService.getAllSensors().map { SensorResponse(it.stationId, SensorType.valueOf(it.sensorType)) }
            ResponseEntity.ok(activeSensors)
        } catch (e: Exception) {
            logger.error(e.message, e)
            ResponseEntity.internalServerError().build()
        }
    }
}