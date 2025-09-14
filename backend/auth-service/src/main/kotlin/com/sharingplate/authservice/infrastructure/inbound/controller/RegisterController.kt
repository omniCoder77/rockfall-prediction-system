package com.sharingplate.authservice.infrastructure.inbound.controller

import com.sharingplate.authservice.domain.exception.UserAlreadyExistsException
import com.sharingplate.authservice.domain.port.driven.RegisterService
import com.sharingplate.authservice.domain.port.driven.TokenService
import com.sharingplate.authservice.infrastructure.inbound.dto.request.RegisterRequest
import com.sharingplate.authservice.infrastructure.inbound.dto.response.RegisterResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/auth/register")
class RegisterController(
    private val registerService: RegisterService,
    private val tokenService: TokenService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping
    fun register(@Valid @RequestBody registerRequest: RegisterRequest): Mono<ResponseEntity<RegisterResponse>> {
        logger.info("Received registration request for email: {}", registerRequest.email)
        return registerService.register(
            registerRequest.name, registerRequest.email, registerRequest.password, registerRequest.phoneNumber
        )
            .flatMap { adminId ->
                val accessToken = tokenService.generateAccessToken(adminId)
                val refreshToken = tokenService.generateRefreshToken(adminId)
                logger.info("Registration successful for admin ID: {}", adminId)
                Mono.just(ResponseEntity.status(HttpStatus.CREATED).body(RegisterResponse(adminId, accessToken, refreshToken)))
            }
            .onErrorResume(UserAlreadyExistsException::class.java) { e ->
                logger.warn("Registration failed due to existing user: {}", e.message)
                Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(RegisterResponse("", "", "")))
            }
            .onErrorResume { error ->
                logger.error("An unexpected error occurred during registration for email {}: {}", registerRequest.email, error.message, error)
                Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RegisterResponse("", "", "")))
            }
    }
}