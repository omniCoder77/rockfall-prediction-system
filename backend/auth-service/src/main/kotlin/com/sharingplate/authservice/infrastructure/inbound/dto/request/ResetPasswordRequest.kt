package com.sharingplate.authservice.infrastructure.inbound.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ResetPasswordRequest(
    val newPassword: String
)