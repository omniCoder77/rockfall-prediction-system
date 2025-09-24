package com.sharingplate.mlservice.application.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig(
    @Value("\${ml.model.base-url}")
    private val mlModelBaseUrl: String
) {

    @Bean
    fun mlModelWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl(mlModelBaseUrl)
            .build()
    }
}