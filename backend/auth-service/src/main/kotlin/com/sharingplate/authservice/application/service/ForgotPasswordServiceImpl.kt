package com.sharingplate.authservice.application.service

import com.sharingplate.authservice.domain.exception.AdminNotFoundException
import com.sharingplate.authservice.domain.exception.InvalidTokenException
import com.sharingplate.authservice.domain.model.Admin
import com.sharingplate.authservice.domain.port.driven.CacheRepository
import com.sharingplate.authservice.domain.port.driven.EmailService
import com.sharingplate.authservice.domain.port.driven.ForgotPasswordService
import com.sharingplate.authservice.domain.port.driven.TokenService
import com.sharingplate.authservice.domain.port.driver.AdminRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.temporal.ChronoUnit

@Service
class ForgotPasswordServiceImpl(
    private val adminRepository: AdminRepository,
    private val tokenService: TokenService,
    private val cacheRepository: CacheRepository,
    private val emailService: EmailService,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${jwt.password-reset.token.expiry-minutes:15}") private val passwordResetTokenExpiryMinutes: Long
) : ForgotPasswordService {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val RESET_TOKEN_PREFIX = "reset_token:"

    override fun requestPasswordReset(email: String): Mono<Boolean> {
        logger.debug("Received password reset request for email: {}", email)
        return adminRepository.findAdminByEmail(email)
            .switchIfEmpty(Mono.error(AdminNotFoundException(email)))
            .flatMap { admin ->
                val resetToken = tokenService.generateSecureToken()
                // Store the token with the admin's email in cache for a limited time (e.g., 15 minutes)
                cacheRepository.store(
                    RESET_TOKEN_PREFIX + resetToken,
                    admin.email,
                    passwordResetTokenExpiryMinutes,
                    ChronoUnit.MINUTES
                )
                    .flatMap { stored ->
                        if (stored) {
                            logger.info("Generated reset token for email {}. Sending email.", email)
                            emailService.sendResetPasswordEmail(admin.email, resetToken)
                                .doOnError { e -> logger.error("Failed to send reset email to {}: {}", email, e.message) }
                        } else {
                            logger.error("Failed to store reset token in cache for email: {}", email)
                            Mono.just(false)
                        }
                    }
            }
            .onErrorResume { e ->
                // Do not expose whether the email exists for security reasons
                if (e is AdminNotFoundException) {
                    logger.info("Password reset requested for non-existent email: {}. Returning success for security.", email)
                    Mono.just(true) // Always return true to avoid user enumeration attacks
                } else {
                    logger.error("Error requesting password reset for email {}: {}", email, e.message, e)
                    Mono.just(false)
                }
            }
    }

    override fun resetPassword(token: String, newPassword: String): Mono<Boolean> {
        logger.debug("Received password reset request with token.")
        return cacheRepository.read(RESET_TOKEN_PREFIX + token, String::class.java)
            .switchIfEmpty(Mono.error(InvalidTokenException()))
            .flatMap { email ->
                logger.info("Valid reset token used for email: {}. Invalidating token.", email)
                cacheRepository.remove(RESET_TOKEN_PREFIX + token)
                    .then(adminRepository.findAdminByEmail(email))
                    .switchIfEmpty(Mono.error(AdminNotFoundException(email)))
                    .flatMap { admin ->
                        val encodedPassword = passwordEncoder.encode(newPassword)
                        adminRepository.updatePassword(admin.adminId, encodedPassword)
                            .map { true }
                            .doOnSuccess { logger.info("Successfully reset password for admin: {}", admin.adminId) }
                            .doOnError { e -> logger.error("Failed to update password for admin {}: {}", admin.adminId, e.message) }
                    }
            }
            .onErrorResume { e ->
                logger.error("Error resetting password with token: {}: {}", token, e.message, e)
                Mono.error(e) // Re-throw custom exceptions for global handler
            }
    }
}