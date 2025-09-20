package com.sharingplate.authservice.application.service

import com.sharingplate.authservice.domain.exception.AdminNotFoundException
import com.sharingplate.authservice.domain.exception.InvalidCredentialsException
import com.sharingplate.authservice.domain.exception.InvalidTokenException
import com.sharingplate.authservice.domain.port.driven.CacheRepository
import com.sharingplate.authservice.domain.port.driven.LoginService
import com.sharingplate.authservice.domain.port.driven.TokenService
import com.sharingplate.authservice.domain.port.driver.AdminRepository
import com.sharingplate.authservice.infrastructure.outbound.communication.SmsOtpAdapter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.temporal.ChronoUnit

@Service
class LoginServiceImpl(
    private val smsOtpAdapter: SmsOtpAdapter,
    private val adminRepository: AdminRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenService: TokenService,
    private val cacheRepository: CacheRepository,
    private val roleService: RoleService, // Inject RoleService
    @Value("\${jwt.token.refresh.key.prefix:refresh_token:}") private val refreshTokenKeyPrefix: String
) : LoginService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun login(phoneNumber: String): Mono<Boolean> {
        logger.info("Initiating OTP login for phone number: {}", phoneNumber)
        return smsOtpAdapter.sendOtp(phoneNumber)
            .doOnError { e -> logger.error("Failed to send OTP to {}: {}", phoneNumber, e.message) }
    }

    override fun login(email: String, password: String): Mono<Pair<String, String>> {
        logger.debug("Attempting login for email: {}", email)
        return adminRepository.findAdminByEmail(email).switchIfEmpty(Mono.error(AdminNotFoundException(email)))
            .flatMap { admin ->
                if (passwordEncoder.matches(password, admin.password)) {
                    val adminId = admin.adminId.toString()
                    val role = roleService.determineRole(admin.jobId).name // Determine role based on jobId
                    val claims = mapOf("roles" to listOf(role)) // Add role to claims

                    val accessToken = tokenService.generateAccessToken(adminId, claims)
                    val refreshToken = tokenService.generateRefreshToken(adminId, claims)

                    cacheRepository.store(
                        refreshTokenKeyPrefix + adminId,
                        refreshToken,
                        tokenService.getRefreshTokenExpiry().toSeconds(),
                        ChronoUnit.SECONDS
                    ).thenReturn(Pair(accessToken, refreshToken))
                        .doOnSuccess { logger.info("Successful login for admin: {} with role: {}", adminId, role) }.doOnError { e ->
                            logger.error(
                                "Failed to store refresh token for admin {}: {}",
                                adminId,
                                e.message
                            )
                        }
                } else {
                    logger.warn("Failed login attempt for email {}: Invalid password", email)
                    Mono.error(InvalidCredentialsException())
                }
            }.doOnError { e -> logger.error("Login attempt failed for email {}: {}", email, e.message) }
    }

    override fun refreshAccessToken(refreshToken: String): Mono<Pair<String, String>> {
        logger.debug("Attempting to refresh access token with refresh token")
        return Mono.justOrEmpty(tokenService.getClaims(refreshToken)) // Get all claims to extract roles
            .switchIfEmpty(Mono.error(InvalidTokenException("Invalid refresh token subject or claims missing.")))
            .flatMap { claims ->
                val adminId = claims.subject
                val roles = claims["roles"] as? List<String> ?: emptyList()
                val roleClaims = mapOf("roles" to roles) // Pass roles back to new token claims

                cacheRepository.read(refreshTokenKeyPrefix + adminId, String::class.java)
                    .switchIfEmpty(Mono.error(InvalidTokenException("Refresh token not found or expired in cache for admin: $adminId")))
                    .flatMap { cachedRefreshToken ->
                        if (cachedRefreshToken == refreshToken) {
                            val newAccessToken = tokenService.generateAccessToken(adminId, roleClaims) // Include roles
                            val newRefreshToken = tokenService.generateRefreshToken(adminId, roleClaims) // Include roles

                            cacheRepository.remove(refreshTokenKeyPrefix + adminId).then(
                                cacheRepository.store(
                                    refreshTokenKeyPrefix + adminId,
                                    newRefreshToken,
                                    tokenService.getRefreshTokenExpiry().toSeconds(),
                                    ChronoUnit.SECONDS
                                )
                            ).thenReturn(Pair(newAccessToken, newRefreshToken))
                                .doOnSuccess { logger.info("Successfully refreshed tokens for admin: {}", adminId) }
                                .doOnError { e ->
                                    logger.error(
                                        "Failed to store new refresh token for admin {}: {}",
                                        adminId,
                                        e.message
                                    )
                                }
                        } else {
                            logger.warn(
                                "Potential refresh token reuse detected for admin {}. Invalidating all tokens.",
                                adminId
                            )
                            cacheRepository.remove(refreshTokenKeyPrefix + adminId)
                                .then(Mono.error(InvalidTokenException("Refresh token mismatch or reuse detected.")))
                        }
                    }
            }.doOnError { e -> logger.error("Token refresh failed: {}", e.message) }
    }

    override fun logout(adminId: String): Mono<Boolean> {
        logger.debug("Attempting to logout admin: {}", adminId)
        return cacheRepository.remove(refreshTokenKeyPrefix + adminId).map { count ->
            if (count > 0) {
                logger.info("Logged out admin {} by removing refresh token from cache.", adminId)
                true
            } else {
                logger.info("Logout for admin {} completed, but no refresh token was found in cache.", adminId)
                false
            }
        }.onErrorResume { e ->
            logger.error("Error during logout for admin {}: {}", adminId, e.message)
            Mono.just(false)
        }
    }
}