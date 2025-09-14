package com.sharingplate.authservice.infrastructure.inbound.dto.response

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String
)