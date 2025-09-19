package com.sharingplate.mineservice.application

import com.sharingplate.mineservice.domain.port.driver.StationRepository
import com.sharingplate.mineservice.infrastructure.persistence.postgres.entity.Station
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class StationService(private val stationRepository: StationRepository) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun getAllStations(): Flux<Station> {
        logger.info("Request to get all stations.")
        return stationRepository.findAllStations()
            .doOnError { e -> logger.error("Service error getting all stations: {}", e.message, e) }
    }

    fun getStationById(stationId: UUID): Mono<Station> {
        logger.info("Request to get station by ID: {}", stationId)
        return stationRepository.findStationById(stationId)
            .doOnError { e -> logger.error("Service error getting station by ID {}: {}", stationId, e.message, e) }
    }

    fun addStation(station: Station): Mono<Station> {
        logger.info("Request to add new station: {}", station.name)
        return stationRepository.addStation(station)
            .doOnError { e -> logger.error("Service error adding station {}: {}", station.name, e.message, e) }
    }

    fun updateStation(station: Station): Mono<Station> {
        logger.info("Request to update station with ID: {}", station.stationId)
        return stationRepository.updateStation(station)
            .doOnError { e -> logger.error("Service error updating station with ID {}: {}", station.stationId, e.message, e) }
    }

    fun deleteStation(stationId: UUID): Mono<Void> {
        logger.info("Request to delete station with ID: {}", stationId)
        return stationRepository.deleteStation(stationId)
            .doOnError { e -> logger.error("Service error deleting station with ID {}: {}", stationId, e.message, e) }
    }
}