package com.sharingplate.authservice.infrastructure.outbound.persistence.redis

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sharingplate.authservice.domain.port.driven.CacheRepository
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.temporal.TemporalUnit

@Component
class RedisCacheRepository(
    private val reactiveRedisOperations: ReactiveRedisOperations<String, Any>,
) : CacheRepository {

    val objectMapper = jacksonObjectMapper()

    override fun store(key: String, data: Any, ttl: Long, unit: TemporalUnit): Mono<Boolean> {
        val data = objectMapper.writeValueAsString(data)
        return reactiveRedisOperations.opsForValue().set(key, data, Duration.of(ttl, unit))
    }

    override fun store(key: String, data: Any): Mono<Boolean> {
        val data = objectMapper.writeValueAsString(data)
        return reactiveRedisOperations.opsForValue().set(key, data)
    }

    override fun remove(key: String): Mono<Long> {
        return reactiveRedisOperations.delete(key)
    }

    override fun <T>read(key: String, klass: Class<T>): Mono<T> {
        return reactiveRedisOperations.opsForValue().get(key).map {
            objectMapper.readValue(it.toString(), klass)
        }
    }
}