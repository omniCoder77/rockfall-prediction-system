package com.sharingplate.authservice.infrastructure.inbound.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class LoginRequest(
    @field:Email(message = "Invalid email format")
    @field:Size(max = 100, message = "Email cannot exceed 100 characters")
    val email: String?,

    @field:Pattern(regexp = "^\\+[1-9]\\d{1,14}\$", message = "Invalid phone number format. Must include country code (e.g., +11234567890)")
    val phoneNumber: String?,

    @field:NotBlank(message = "Password cannot be blank")
    val password: String
)
