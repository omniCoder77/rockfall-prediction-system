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
    private val passwordEncoder: PasswordEncoder,
    private val roleService: RoleService // Inject RoleService
) : RegisterService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun register(
        name: String, email: String, password: String, phoneNumber: String, jobId: String
    ): Mono<Pair<String, String>> {
        logger.debug(
            "Attempting to register new admin with email: {}, phone: {}, and job ID: {}",
            email,
            phoneNumber,
            jobId
        )
        val encodedPassword = passwordEncoder.encode(password)
        val newAdmin =
            Admin(name = name, email = email, password = encodedPassword, phoneNumber = phoneNumber, jobId = jobId)

        return adminRepository.findAdminByEmail(email)
            .flatMap { Mono.error<Pair<String, String>>(UserAlreadyExistsException("email '$email'")) }.switchIfEmpty(
                adminRepository.findAdminByPhoneNumber(phoneNumber).map { Pair(it.email, it.adminId.toString()) })
            .flatMap { Mono.error<Pair<String, String>>(UserAlreadyExistsException("phone number '$phoneNumber'")) }
            .switchIfEmpty(adminRepository.findAdminByJobId(jobId).map { Pair(it.email, it.adminId.toString()) })
            .flatMap { Mono.error<Pair<String, String>>(UserAlreadyExistsException("job ID '$jobId'")) }.switchIfEmpty(
                Mono.defer {
                    val role = roleService.determineRole(jobId).name
                    adminRepository.insert(newAdmin).doOnSuccess { adminId ->
                            logger.info(
                                "Successfully registered new admin with ID: {} and role: {}",
                                adminId,
                                role
                            )
                        }
                        .doOnError { e -> logger.error("Failed to register admin with email {}: {}", email, e.message) }
                        .map { adminId -> Pair(adminId, role) }
                }).onErrorResume { e ->
                if (e is UserAlreadyExistsException) {
                    Mono.error(e)
                } else {
                    Mono.error(RuntimeException("Failed to register user: ${e.message}", e))
                }
            }
    }
}