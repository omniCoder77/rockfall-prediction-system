package com.sharingplate.authservice.infrastructure.outbound.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.HandlerFilterFunction
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.net.InetSocketAddress
import java.time.LocalTime
import java.util.*

@Component
class RateLimiterHandlerFilterFunction(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, Long>,
    @Value("\${MAX_REQUESTS_PER_MINUTE}") private val maxRequestPerMinute: Long,
    private val script: RedisScript<Boolean>
) : HandlerFilterFunction<ServerResponse, ServerResponse> {
    override fun filter(request: ServerRequest, next: HandlerFunction<ServerResponse?>): Mono<ServerResponse?> {
        val currentMinute = LocalTime.now().minute
        val key = String.format("rl_%s:%s", requestAddress(request.remoteAddress()), currentMinute)

        return reactiveRedisTemplate
            .execute(script, listOf(key), listOf(maxRequestPerMinute, 59))
            .single(false)
            .flatMap({ value ->
                if (value)
                    ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS).build() else
                    next.handle(request)
            })
    }

    private fun requestAddress(maybeAddress: Optional<InetSocketAddress>): String? {
        return if (maybeAddress.isPresent) maybeAddress.get().hostName else ""
    }
}