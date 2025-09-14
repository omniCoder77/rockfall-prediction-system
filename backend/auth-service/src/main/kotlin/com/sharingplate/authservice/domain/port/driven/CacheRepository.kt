package com.sharingplate.authservice.domain.port.driven

import reactor.core.publisher.Mono
import java.time.temporal.TemporalUnit
import java.util.concurrent.TimeUnit

interface CacheRepository {
    fun store(key: String, data: Any, ttl: Long, unit: TemporalUnit): Mono<Boolean>
    fun store(key: String, data: Any): Mono<Boolean>
    fun remove(key: String): Mono<Long>
    fun <T>read(key: String, klass: Class<T>): Mono<T>
}