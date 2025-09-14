package com.sharingplate.authservice.domain.port.driven

import reactor.core.publisher.Mono

interface EmailService {
    fun sendResetPasswordEmail(to: String, token: String): Mono<Boolean>
}