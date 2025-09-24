package com.sharingplate.mineservice.infrastructure.persistence.postgres

import com.sharingplate.mineservice.domain.port.driver.StationRepository
import com.sharingplate.mineservice.infrastructure.persistence.postgres.entity.Station
import org.slf4j.LoggerFactory
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Component
class StationRepositoryImpl(private val r2dbcEntityTemplate: R2dbcEntityTemplate) : StationRepository {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun findAllStations(): Flux<Station> {
        logger.debug("Fetching all stations from the database.")
        return r2dbcEntityTemplate.select(Station::class.java).all()
            .doOnComplete { logger.debug("Finished fetching all stations.") }
            .doOnError { e -> logger.error("Error fetching all stations: {}", e.message, e) }
    }

    override fun findStationById(stationId: UUID): Mono<Station> {
        logger.debug("Fetching station by ID: {}", stationId)
        return r2dbcEntityTemplate.select(
            Query.query(Criteria.where("station_id").`is`(stationId)),
            Station::class.java
        ).singleOrEmpty().doOnSuccess { station ->
                if (station != null) logger.debug("Found station: {}", station.name)
                else logger.debug("Station with ID {} not found.", stationId)
            }.doOnError { e -> logger.error("Error fetching station by ID {}: {}", stationId, e.message, e) }
    }

    override fun addStation(station: Station): Mono<Station> {
        logger.debug("Adding new station: {}", station.name)
        return r2dbcEntityTemplate.insert(station).doOnSuccess { addedStation ->
                logger.info(
                    "Successfully added station with ID: {}",
                    addedStation.stationId
                )
            }.doOnError { e -> logger.error("Error adding station {}: {}", station.name, e.message, e) }
    }

    override fun updateStation(station: Station): Mono<Station> {
        logger.debug("Updating station with ID: {}", station.stationId)
        return r2dbcEntityTemplate.update(station).doOnSuccess { updatedStation ->
                logger.info(
                    "Successfully updated station with ID: {}",
                    updatedStation.stationId
                )
            }.doOnError { e -> logger.error("Error updating station with ID {}: {}", station.stationId, e.message, e) }
    }

    override fun deleteStation(stationId: UUID): Mono<Void> {
        logger.debug("Deleting station with ID: {}", stationId)
        return r2dbcEntityTemplate.delete(
            Query.query(Criteria.where("station_id").`is`(stationId)),
            Station::class.java
        ).then() // Convert to Mono<Void> as delete returns Mono<Integer>
            .doOnSuccess { logger.info("Successfully deleted station with ID: {}", stationId) }
            .doOnError { e -> logger.error("Error deleting station with ID {}: {}", stationId, e.message, e) }
    }


    override fun findStationsByRiskLevelBetween(minRiskLevel: Double, maxRiskLevel: Double): Flux<Station> {
        logger.debug("Fetching stations with risk level between {} and {}", minRiskLevel, maxRiskLevel)
        val criteria = Criteria.where("risk_level").between(minRiskLevel, maxRiskLevel)
        return r2dbcEntityTemplate.select(Query.query(criteria), Station::class.java)
            .doOnComplete { logger.debug("Finished fetching stations with risk level between {} and {}.", minRiskLevel, maxRiskLevel) }
            .doOnError { e -> logger.error("Error fetching stations by risk level range: {}", e.message, e) }
    }
}