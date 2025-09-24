package com.sharingplate.mineservice.domain.port.driver

import com.sharingplate.mineservice.infrastructure.persistence.postgres.entity.Station
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface StationRepository {
    fun findAllStations(): Flux<Station>
    fun findStationById(stationId: UUID): Mono<Station>
    fun addStation(station: Station): Mono<Station>
    fun updateStation(station: Station): Mono<Station>
    fun deleteStation(stationId: UUID): Mono<Void>
    fun findStationsByRiskLevelBetween(minRiskLevel: Double, maxRiskLevel: Double): Flux<Station>
}