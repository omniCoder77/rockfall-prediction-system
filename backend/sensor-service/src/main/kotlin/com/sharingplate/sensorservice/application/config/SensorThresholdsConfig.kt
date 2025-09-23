package com.sharingplate.sensorservice.application.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "sensor.fluctuation-thresholds")
class SensorThresholdsConfig {
    var thresholds: Map<String, Map<String, Double>> = mutableMapOf()
}