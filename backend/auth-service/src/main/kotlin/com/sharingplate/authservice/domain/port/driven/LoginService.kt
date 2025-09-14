package com.sharingplate.authservice.domain.port.driven

import reactor.core.publisher.Mono

interface LoginService {
    fun login(phoneNumber: String): Mono<Boolean>
    fun login(email: String, password: String): Mono<Pair<String, String>>
    fun refreshAccessToken(refreshToken: String): Mono<Pair<String, String>> // New method
    fun logout(adminId: String): Mono<Boolean> // New method
}