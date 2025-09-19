package com.sharingplate.mineservice.infrastructure.persistence.postgres.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("station")
data class Station(
    @Id val stationId: UUID = UUID.randomUUID(), // Mark as ID and provide a default for new stations
    val name: String,
    val siteEngineer: UUID,
    val riskLevel: Double
)