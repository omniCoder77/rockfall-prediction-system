package com.sharingplate.sensorservice.infrastructure.inbound.web

import com.sharingplate.sensorservice.application.service.SensorHistoryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/history")
class SensorHistoryController(private val sensorHistoryService: SensorHistoryService) {

    @GetMapping("/sensors")
    fun getAllSensorsHistory(
        @RequestParam(defaultValue = "-1d") start: String,
        @RequestParam(defaultValue = "now()") stop: String,
        @RequestParam(defaultValue = "1h") interval: String
    ): ResponseEntity<*> {
        return try {
            val history = sensorHistoryService.getAllSensorsHistory(start, stop, interval)
            ResponseEntity.ok(history)
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(e.message)
        }
    }

    @GetMapping("/sensors/station/{stationId}")
    fun getStationSensorsHistory(
        @PathVariable stationId: String,
        @RequestParam(defaultValue = "-1d") start: String,
        @RequestParam(defaultValue = "now()") stop: String,
        @RequestParam(defaultValue = "1h") interval: String
    ): ResponseEntity<*> {
        return try {
            val history = sensorHistoryService.getStationSensorsHistory(stationId, start, stop, interval)
            ResponseEntity.ok(history)
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(e.message)
        }
    }

    @GetMapping("/sensors/type/{sensorType}")
    fun getSensorTypeHistory(
        @PathVariable sensorType: String,
        @RequestParam(required = false) stationId: String?,
        @RequestParam(defaultValue = "-1d") start: String,
        @RequestParam(defaultValue = "now()") stop: String,
        @RequestParam(defaultValue = "1h") interval: String
    ): ResponseEntity<*> {
        return try {
            val history = sensorHistoryService.getSensorHistory(sensorType, stationId, start, stop, interval)
            ResponseEntity.ok(history)
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(e.message)
        }
    }
}