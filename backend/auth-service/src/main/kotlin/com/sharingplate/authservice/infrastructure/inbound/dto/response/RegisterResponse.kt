package com.sharingplate.authservice.infrastructure.inbound.dto.response

data class RegisterResponse(
    val userId: String,
    val accessToken: String,
    val refreshToken: String
)
