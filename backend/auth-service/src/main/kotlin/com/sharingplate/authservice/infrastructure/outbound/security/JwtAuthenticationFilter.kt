package com.sharingplate.authservice.infrastructure.outbound.security

import com.sharingplate.authservice.domain.port.driven.TokenService
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationFilter(
    private val userDetailsService: AuthUserDetailService, private val tokenService: TokenService
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange)
        }

        val token = authHeader.removePrefix("Bearer ")

        return Mono.justOrEmpty(tokenService.getSubject(token)).flatMap { username ->
            userDetailsService.findByUsername(username).flatMap { userDetails ->
                val authentication = UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.authorities
                )
                chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
            }
        }
            .switchIfEmpty(chain.filter(exchange))
    }

}