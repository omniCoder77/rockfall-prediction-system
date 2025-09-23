package com.sharingplate.authservice.infrastructure.outbound.security

import com.sharingplate.authservice.application.service.RoleService
import com.sharingplate.authservice.domain.exception.AdminNotFoundException
import com.sharingplate.authservice.domain.port.driver.AdminRepository
import org.slf4j.LoggerFactory
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*

@Component
class AuthUserDetailService(
    private val adminRepository: AdminRepository, private val roleService: RoleService
) : ReactiveUserDetailsService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun findByUsername(username: String): Mono<UserDetails> {
        logger.debug("Attempting to find user by username (ID or Email): {}", username)
        val findAdminMono = try {
            val uuid = UUID.fromString(username)
            adminRepository.findAdminById(uuid).switchIfEmpty(Mono.error(AdminNotFoundException(uuid)))
        } catch (e: IllegalArgumentException) {
            adminRepository.findAdminByEmail(username).switchIfEmpty(Mono.error(AdminNotFoundException(username)))
        }

        return findAdminMono.map { admin ->
                val role = roleService.determineRole(admin.jobId)
                val authorities: List<GrantedAuthority> = listOf(SimpleGrantedAuthority("ROLE_${role.name}"))
                org.springframework.security.core.userdetails.User(
                    admin.adminId.toString(), admin.password, authorities
                )
            }.cast(UserDetails::class.java).doOnSuccess { userDetails ->
                logger.debug(
                    "Found admin and determined roles for user: {}",
                    userDetails.username
                )
            }.doOnError { e -> logger.warn("Failed to find admin or determine roles for {}: {}", username, e.message) }
    }
}