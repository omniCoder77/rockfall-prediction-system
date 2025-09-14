package com.sharingplate.authservice.infrastructure.inbound.controller

import com.sharingplate.authservice.domain.exception.InvalidTokenException
import com.sharingplate.authservice.domain.port.driven.LoginService
import com.sharingplate.authservice.infrastructure.inbound.dto.request.RefreshTokenRequest
import com.sharingplate.authservice.infrastructure.inbound.dto.response.RefreshTokenResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/auth/token")
class TokenController(
    private val loginService: LoginService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/refresh")
    fun refreshTokens(@Valid @RequestBody request: RefreshTokenRequest): Mono<ResponseEntity<RefreshTokenResponse>> {
        logger.info("Received request to refresh tokens.")
        return loginService.refreshAccessToken(request.refreshToken)
            .map { (accessToken, newRefreshToken) ->
                ResponseEntity.ok(RefreshTokenResponse(accessToken, newRefreshToken))
            }
            .doOnSuccess { logger.info("Tokens refreshed successfully.") }
            .onErrorResume(InvalidTokenException::class.java) { e ->
                logger.warn("Token refresh failed: {}", e.message)
                Mono.just(ResponseEntity.status(401).body(RefreshTokenResponse("", "")))
            }
            .onErrorResume { e ->
                logger.error("An unexpected error occurred during token refresh: {}", e.message, e)
                Mono.just(ResponseEntity.status(500).body(RefreshTokenResponse("", "")))
            }
    }
}