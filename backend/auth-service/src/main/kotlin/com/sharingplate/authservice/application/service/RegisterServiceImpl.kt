package com.sharingplate.authservice.application.service

import com.sharingplate.authservice.domain.exception.UserAlreadyExistsException
import com.sharingplate.authservice.domain.model.Admin
import com.sharingplate.authservice.domain.port.driven.RegisterService
import com.sharingplate.authservice.domain.port.driver.AdminRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class RegisterServiceImpl(
    private val adminRepository: AdminRepository,
    private val passwordEncoder: PasswordEncoder
) : RegisterService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun register(
        name: String, email: String, password: String, phoneNumber: String
    ): Mono<String> {
        logger.debug("Attempting to register new admin with email: {} and phone: {}", email, phoneNumber)
        val encodedPassword = passwordEncoder.encode(password)
        val newAdmin = Admin(name = name, email = email, password = encodedPassword, phoneNumber = phoneNumber)

        return adminRepository.findAdminByEmail(email)
            .flatMap { Mono.error<String>(UserAlreadyExistsException("email '$email'")) }
            .switchIfEmpty(
                adminRepository.insert(newAdmin)
                    .doOnSuccess { adminId -> logger.info("Successfully registered new admin with ID: {}", adminId) }
                    .doOnError { e -> logger.error("Failed to register admin with email {}: {}", email, e.message) }
            )
            .cast(String::class.java)
            .onErrorResume { e ->
                if (e is UserAlreadyExistsException) {
                    Mono.error(e)
                } else {
                    adminRepository.findAdminByPhoneNumber(phoneNumber)
                        .flatMap { Mono.error<String>(UserAlreadyExistsException("phone number '$phoneNumber'")) }
                        .switchIfEmpty(Mono.error(RuntimeException("Failed to register user: ${e.message}")))
                        .cast(String::class.java)
                }
            }
    }
}