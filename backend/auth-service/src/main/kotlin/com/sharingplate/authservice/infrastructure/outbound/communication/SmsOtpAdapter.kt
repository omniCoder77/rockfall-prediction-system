package com.sharingplate.authservice.infrastructure.outbound.communication

import com.sharingplate.authservice.domain.port.driven.OtpDeliveryService
import com.twilio.rest.verify.v2.service.Verification
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class SmsOtpAdapter(
    @Value("\${twilio.path-service-id}") private val pathServiceId: String
) : OtpDeliveryService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun sendOtp(phoneNumber: String): Mono<Boolean> {
        return try {
            Mono.fromCallable {
                val verification = Verification.creator(pathServiceId, phoneNumber, "sms").create()
                logger.info("Sending verification to $phoneNumber", verification.accountSid)
            }.subscribeOn(Schedulers.boundedElastic()).map { true }
        } catch (e: Exception) {
            Mono.just(false)
        }
    }
}