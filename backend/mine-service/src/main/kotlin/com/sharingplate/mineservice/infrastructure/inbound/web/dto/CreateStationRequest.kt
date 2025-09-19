package com.sharingplate.mineservice.infrastructure.inbound.web.dto

import java.util.UUID

data class CreateStationRequest(
    val name: String,
    val siteEngineer: String,
)