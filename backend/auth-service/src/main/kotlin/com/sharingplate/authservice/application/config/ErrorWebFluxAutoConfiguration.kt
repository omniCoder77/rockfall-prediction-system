package com.sharingplate.authservice.application.config

import com.sharingplate.authservice.infrastructure.inbound.handler.GlobalErrorAttributes
import org.springframework.boot.autoconfigure.web.ErrorProperties
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.codec.ServerCodecConfigurer

@Configuration
class ErrorWebFluxAutoConfiguration {

    @Bean
    @Order(-1)
    fun errorWebExceptionHandler(
        errorAttributes: ErrorAttributes,
        webProperties: WebProperties,
        applicationContext: ApplicationContext,
        serverCodecConfigurer: ServerCodecConfigurer
    ): DefaultErrorWebExceptionHandler {
        val errorProperties = ErrorProperties()
        val exceptionHandler = DefaultErrorWebExceptionHandler(
            errorAttributes, webProperties.resources, errorProperties, applicationContext
        )
        exceptionHandler.setMessageWriters(serverCodecConfigurer.writers)
        exceptionHandler.setMessageReaders(serverCodecConfigurer.readers)
        return exceptionHandler
    }

    @Bean
    fun globalErrorAttributes(): GlobalErrorAttributes {
        return GlobalErrorAttributes()
    }
}