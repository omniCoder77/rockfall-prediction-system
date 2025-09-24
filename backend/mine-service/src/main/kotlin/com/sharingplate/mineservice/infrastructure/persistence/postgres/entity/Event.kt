package com.sharingplate.mineservice.infrastructure.persistence.postgres.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("events")
data class Event(
    @Id val eventId: UUID = UUID.randomUUID(),
    val name: String,
    val eventStatus: String,
    val description: String? = null,
    val assignedSupervisorId: UUID? = null,
    val assignedSuperVisorName: String? = null,
)
