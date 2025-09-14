package com.sharingplate.authservice.infrastructure.outbound.communication

import com.sharingplate.authservice.domain.port.driven.EmailService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class EmailServiceImpl : EmailService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun sendResetPasswordEmail(to: String, token: String): Mono<Boolean> {
        return Mono.fromCallable {
            logger.info("Sending password reset email to $to with token: $token")
            // In a real application, integrate with an email sending service like SendGrid, Mailgun, etc.
            true
        }.onErrorResume {
            logger.error("Failed to send password reset email to $to", it)
            Mono.just(false)
        }
    }
}