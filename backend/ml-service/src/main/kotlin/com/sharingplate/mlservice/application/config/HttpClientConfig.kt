package com.sharingplate.mlservice.application.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

@Configuration
class HttpClientConfig {
    
    @Bean
    fun restTemplate(): RestTemplate {
        val factory = SimpleClientHttpRequestFactory()
        factory.setConnectTimeout(5000)  // 5 seconds
        factory.setReadTimeout(10000)    // 10 seconds
        return RestTemplate(factory)
    }
}
