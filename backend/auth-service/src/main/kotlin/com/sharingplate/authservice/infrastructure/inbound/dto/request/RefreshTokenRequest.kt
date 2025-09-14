package com.sharingplate.authservice.infrastructure.inbound.dto.request

import jakarta.validation.constraints.NotBlank

data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token cannot be blank")
    val refreshToken: String
)