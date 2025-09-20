package com.sharingplate.authservice.application.config

import com.sharingplate.authservice.infrastructure.outbound.security.JwtAuthenticationFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    @Value("\${passwordEncoder.strength}") private val passwordEncoderStrength: Int,
    @Value("\${cors.allowed-origins:*}") private val allowedOrigins: List<String>,
    @Value("\${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}") private val allowedMethods: List<String>,
    @Value("\${cors.allowed-headers:*}") private val allowedHeaders: List<String>,
    @Value("\${cors.allow-credentials:true}") private val allowCredentials: Boolean,
) : WebFluxConfigurer {

    @Bean
    fun securityWebFilterChain(
        http: ServerHttpSecurity, jwtAuthenticationFilter: JwtAuthenticationFilter
    ): SecurityWebFilterChain {
        return http.httpBasic { it.disable() }
            .formLogin { it.disable() }
            .csrf { it.disable() }
            .authorizeExchange { exchanges ->
                exchanges.pathMatchers(
                    "/api/v1/auth/register",
                    "/api/v1/auth/login",
                    "/api/v1/auth/password/**",
                    "/api/v1/auth/token/refresh",
                    "/actuator/**"
                ).permitAll()
                exchanges.pathMatchers(HttpMethod.GET, "/api/v1/some-resource-for-supervisor").hasAnyRole("SUPERVISOR", "ADMIN")
                exchanges.pathMatchers(HttpMethod.POST, "/api/v1/admin-only-resource").hasRole("ADMIN")
                exchanges.anyExchange().authenticated()
            }
            .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(passwordEncoderStrength)
    }

    @Bean
    fun corsWebFilter(): CorsWebFilter {
        val corsConfig = CorsConfiguration().apply {
            allowedOrigins?.forEach { addAllowedOrigin(it) }
            allowedMethods?.forEach { addAllowedMethod(it) }
            allowedHeaders?.forEach { addAllowedHeader(it) }
            allowCredentials = this@SecurityConfig.allowCredentials
            maxAge = 3600L
        }
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", corsConfig)
        return CorsWebFilter(source)
    }
}