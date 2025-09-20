package com.sharingplate.mlservice.infrastructure.outbound.mlapi

data class ModelHealth(
    val status: String,
    val message: String? = null,
    val timestamp: String
)
