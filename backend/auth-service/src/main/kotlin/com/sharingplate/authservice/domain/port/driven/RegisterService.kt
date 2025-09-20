package com.sharingplate.authservice.domain.port.driven

import reactor.core.publisher.Mono

interface RegisterService {
    fun register(name: String, email: String, password: String, phoneNumber: String, jobId: String): Mono<Pair<String, String>>
}