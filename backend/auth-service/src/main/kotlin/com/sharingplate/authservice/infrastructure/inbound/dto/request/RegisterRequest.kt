package com.sharingplate.authservice.infrastructure.inbound.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class RegisterRequest(
    val name: String,
    val phoneNumber: String,
    val email: String,
    val password: String,
    val jobId: String
)