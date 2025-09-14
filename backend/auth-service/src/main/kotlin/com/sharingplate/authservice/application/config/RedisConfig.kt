package com.sharingplate.authservice.application.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.data.redis.serializer.*

@Configuration
class RedisConfig {


    @Bean
    fun reactiveRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, Long> {
        val jdkSerializationRedisSerializer = JdkSerializationRedisSerializer()
        val stringRedisSerializer = StringRedisSerializer.UTF_8
        val longToStringSerializer = GenericToStringSerializer(Long::class.java)
        val template = ReactiveRedisTemplate(
            factory,
            RedisSerializationContext.newSerializationContext<String, Long>(jdkSerializationRedisSerializer)
                .key(stringRedisSerializer).value(longToStringSerializer).build()
        )
        return template
    }

    @Bean
    fun redisOperations(factory: ReactiveRedisConnectionFactory): ReactiveRedisOperations<String, Any> {
        val serializer: Jackson2JsonRedisSerializer<Any> = Jackson2JsonRedisSerializer(Any::class.java)

        val builder: RedisSerializationContext.RedisSerializationContextBuilder<String, Any> =
            RedisSerializationContext.newSerializationContext(StringRedisSerializer())

        val context: RedisSerializationContext<String?, Any> = builder.value(serializer).build()

        return ReactiveRedisTemplate<String, Any>(factory, context)
    }

    @Bean
    fun script(): RedisScript<Boolean> {
        return RedisScript.of(ClassPathResource("scripts/rateLimiter.lua"), Boolean::class.java)
    }
}