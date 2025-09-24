package com.sharingplate.mineservice.infrastructure.inbound.web

import com.sharingplate.mineservice.application.StationService
import com.sharingplate.mineservice.infrastructure.inbound.web.dto.CreateStationRequest
import com.sharingplate.mineservice.infrastructure.persistence.postgres.entity.Station
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("/api/v1/mine/stations")
class StationController(private val stationService: StationService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/stations-by-risk")
    fun getStationIdsByRiskLevel(
        @RequestParam minRiskLevel: Double,
        @RequestParam maxRiskLevel: Double
    ): Flux<UUID> {
        logger.info("Received request for station IDs with risk level between {} and {}", minRiskLevel, maxRiskLevel)
        return stationService.getStationIdsByRiskLevelBetween(minRiskLevel, maxRiskLevel)
            .doOnError { e -> logger.error("API error fetching station IDs by risk range: {}", e.message, e) }
    }

    @GetMapping("/{stationId}")
    fun getStationById(@PathVariable stationId: UUID): Mono<ResponseEntity<Station>> {
        logger.info("Received request to fetch station by ID: {}", stationId)
        return stationService.getStationById(stationId).map { station -> ResponseEntity.ok(station) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
            .doOnError { e -> logger.error("API error fetching station by ID {}: {}", stationId, e.message, e) }
    }

    @PostMapping
    fun addStation(@RequestBody station: CreateStationRequest): Mono<ResponseEntity<Station>> {
        logger.info("Received request to add new station: {}", station.name)
        val station = Station(name = station.name, siteEngineer = UUID.fromString(station.siteEngineer), riskLevel = 0.0)
        return stationService.addStation(station)
            .map { addedStation -> ResponseEntity.status(HttpStatus.CREATED).body(addedStation) }
            .doOnError { e -> logger.error("API error adding station {}: {}", station.name, e.message, e) }
    }

    @PutMapping("/{stationId}")
    fun updateStation(@PathVariable stationId: UUID, @RequestBody station: Station): Mono<ResponseEntity<Station>> {
        logger.info("Received request to update station with ID: {}", stationId)
        if (station.stationId != stationId) {
            logger.warn("Station ID in path ({}) does not match station ID in body ({}).", stationId, station.stationId)
            return Mono.just(ResponseEntity.badRequest().build())
        }
        return stationService.updateStation(station).map { updatedStation -> ResponseEntity.ok(updatedStation) }
            .defaultIfEmpty(ResponseEntity.notFound().build()) // In case the station to update doesn't exist
            .doOnError { e -> logger.error("API error updating station with ID {}: {}", stationId, e.message, e) }
    }

    @DeleteMapping("/{stationId}")
    fun deleteStation(@PathVariable stationId: UUID): Mono<ResponseEntity<Boolean>> {
        logger.info("Received request to delete station with ID: {}", stationId)
        return stationService.deleteStation(stationId).then(Mono.just(ResponseEntity.ok(true))).onErrorResume { e ->
            logger.error("API error deleting station with ID {}: {}", stationId, e.message, e)
            Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
        }
    }
}