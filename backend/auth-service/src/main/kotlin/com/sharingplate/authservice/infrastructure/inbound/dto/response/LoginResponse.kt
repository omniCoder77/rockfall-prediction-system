package com.sharingplate.authservice.infrastructure.inbound.dto.response

sealed interface LoginResponse {
    data class Token(val accessToken: String, val refreshToken: String) : LoginResponse
    data object OTP: LoginResponse
    data class Failure(val error: String) : LoginResponse
}