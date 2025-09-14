package com.sharingplate.authservice.infrastructure.inbound.controller

import com.sharingplate.authservice.domain.exception.InvalidCredentialsException
import com.sharingplate.authservice.domain.port.driven.LoginService
import com.sharingplate.authservice.infrastructure.inbound.dto.request.LoginRequest
import com.sharingplate.authservice.infrastructure.inbound.dto.response.LoginResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/auth")
class LoginController(private val loginService: LoginService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequest): Mono<ResponseEntity<LoginResponse>> {
        val validationError = validateLoginRequest(loginRequest)
        if (validationError != null) {
            logger.warn("Login request validation failed: {}", validationError)
            return Mono.just(ResponseEntity.badRequest().body(LoginResponse.Failure(validationError)))
        }

        return if (loginRequest.email != null && loginRequest.password.isNotBlank()) {
            loginService.login(loginRequest.email, loginRequest.password)
                .map { (accessToken, refreshToken) ->
                    logger.info("Email login successful for email: {}", loginRequest.email)
                    ResponseEntity.ok(LoginResponse.Token(accessToken = accessToken, refreshToken = refreshToken) as LoginResponse)
                }
                .onErrorResume(InvalidCredentialsException::class.java) { e ->
                    logger.warn("Email login failed for {}: {}", loginRequest.email, e.message)
                    Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(LoginResponse.Failure(e.message ?: "Invalid credentials")))
                }
        } else if (loginRequest.phoneNumber != null) {
            loginService.login(loginRequest.phoneNumber)
                .map { success ->
                    if (success) {
                        logger.info("OTP request sent successfully to phone number: {}", loginRequest.phoneNumber)
                        ResponseEntity.ok(LoginResponse.OTP as LoginResponse) // Explicitly cast
                    } else {
                        logger.error("Failed to send OTP to phone number: {}", loginRequest.phoneNumber)
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(LoginResponse.Failure("Failed to send OTP"))
                    }
                }
        } else {
            logger.error("Invalid login request: neither email/password nor phone number provided after validation.")
            Mono.just(ResponseEntity.badRequest().body(LoginResponse.Failure("Invalid login request")))
        }
    }
    @PostMapping("/logout")
    fun logout(@AuthenticationPrincipal userDetails: UserDetails): Mono<ResponseEntity<String>> {
        val adminId = userDetails.username
        logger.info("Logout request received for admin: {}", adminId)
        return loginService.logout(adminId)
            .map { success ->
                if (success) {
                    logger.info("Admin {} logged out successfully.", adminId)
                    ResponseEntity.ok("Logged out successfully.")
                } else {
                    logger.warn("Logout for admin {} completed, but no active session found or refresh token removed.", adminId)
                    ResponseEntity.ok("No active session found or already logged out.")
                }
            }
            .onErrorResume { e ->
                logger.error("Error during logout for admin {}: {}", adminId, e.message, e)
                Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to logout."))
            }
    }

    private fun validateLoginRequest(request: LoginRequest): String? {
        return when {
            request.email.isNullOrBlank() && request.phoneNumber.isNullOrBlank() -> {
                "Either email or phone number is required"
            }
            !request.email.isNullOrBlank() && !request.phoneNumber.isNullOrBlank() -> {
                "Provide either email or phone number, not both"
            }
            !request.email.isNullOrBlank() && request.password.isBlank() -> {
                "Password is required for email login"
            }
            else -> null
        }
    }
}