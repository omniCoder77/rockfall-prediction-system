package com.sharingplate.authservice.infrastructure.inbound.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ForgotPasswordRequest(
    @field:NotBlank(message = "Email cannot be blank")
    @field:Email(message = "Invalid email format")
    @field:Size(max = 100, message = "Email cannot exceed 100 characters")
    val email: String
)