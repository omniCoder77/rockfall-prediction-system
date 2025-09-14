package com.sharingplate.authservice.infrastructure.inbound.controller

import com.sharingplate.authservice.domain.exception.AdminNotFoundException
import com.sharingplate.authservice.domain.exception.InvalidTokenException
import com.sharingplate.authservice.domain.port.driven.ForgotPasswordService
import com.sharingplate.authservice.infrastructure.inbound.dto.request.ForgotPasswordRequest
import com.sharingplate.authservice.infrastructure.inbound.dto.request.ResetPasswordRequest
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RequestMapping("/api/v1/auth/password")
@RestController
class ForgotPasswordController(
    private val forgotPasswordService: ForgotPasswordService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/request")
    fun requestPasswordReset(@Valid @RequestBody request: ForgotPasswordRequest): Mono<ResponseEntity<String>> {
        logger.info("Received request to initiate password reset for email: {}", request.email)
        return forgotPasswordService.requestPasswordReset(request.email)
            .map { success ->
                logger.info("Password reset request processed for email {}. Success: {}", request.email, success)
                ResponseEntity.ok("If an account with that email exists, a password reset link has been sent.")
            }
            .onErrorResume { e ->
                logger.error("An unexpected error occurred during password reset request for email {}: {}", request.email, e.message, e)
                Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during password reset request."))
            }
    }

    @PostMapping("/reset")
    fun resetPassword(@RequestParam("token") token: String, @Valid @RequestBody request: ResetPasswordRequest): Mono<ResponseEntity<String>> {
        logger.info("Received request to reset password with token.")
        return forgotPasswordService.resetPassword(token, request.newPassword)
            .map { success ->
                if (success) {
                    logger.info("Password successfully reset using token.")
                    ResponseEntity.ok("Password has been reset successfully.")
                } else {
                    logger.warn("Password reset failed for token (internal issue).")
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to reset password.")
                }
            }
            .onErrorResume(InvalidTokenException::class.java) { e ->
                logger.warn("Password reset failed: {}", e.message)
                Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message))
            }
            .onErrorResume(AdminNotFoundException::class.java) { e ->
                logger.warn("Password reset failed: {}", e.message)
                Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token, or user not found."))
            }
            .onErrorResume { e ->
                logger.error("An unexpected error occurred during password reset with token: {}: {}", token, e.message, e)
                Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during password reset."))
            }
    }
}