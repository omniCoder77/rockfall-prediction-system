package com.sharingplate.authservice.domain.port.driven

import reactor.core.publisher.Mono

interface ForgotPasswordService {
    fun requestPasswordReset(email: String): Mono<Boolean>
    fun resetPassword(token: String, newPassword: String): Mono<Boolean>
}