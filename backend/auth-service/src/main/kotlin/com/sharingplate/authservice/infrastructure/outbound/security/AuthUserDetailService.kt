package com.sharingplate.authservice.infrastructure.outbound.security

import com.sharingplate.authservice.domain.exception.AdminNotFoundException
import com.sharingplate.authservice.domain.port.driver.AdminRepository
import com.sharingplate.authservice.infrastructure.outbound.persistence.postgres.entity.toEntity
import org.slf4j.LoggerFactory
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*

@Component
class AuthUserDetailService(
    private val adminRepository: AdminRepository,
) : ReactiveUserDetailsService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun findByUsername(username: String): Mono<UserDetails> {
        logger.debug("Attempting to find user by username (ID or Email): {}", username)
        return try {
            val uuid = UUID.fromString(username)
            adminRepository.findAdminById(uuid)
                .map { it.toEntity() }
                .cast(UserDetails::class.java)
                .switchIfEmpty(Mono.error(AdminNotFoundException(uuid)))
                .doOnSuccess { userDetails -> logger.debug("Found admin by ID: {}", userDetails.username) }
                .doOnError { e -> logger.warn("Failed to find admin by ID {}: {}", username, e.message) }
        } catch (e: IllegalArgumentException) {
            adminRepository.findAdminByEmail(username)
                .map { it.toEntity() }
                .cast(UserDetails::class.java)
                .switchIfEmpty(Mono.error(AdminNotFoundException(username)))
                .doOnSuccess { userDetails -> logger.debug("Found admin by email: {}", userDetails.username) }
                .doOnError { error -> logger.warn("Failed to find admin by email {}: {}", username, error.message) }
        }
    }
}