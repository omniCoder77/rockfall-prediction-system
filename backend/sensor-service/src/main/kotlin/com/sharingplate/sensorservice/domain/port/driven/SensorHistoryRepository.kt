package com.sharingplate.sensorservice.domain.port.driven

import com.sharingplate.sensorservice.domain.model.SensorDataPoint

interface SensorHistoryRepository {
    fun getHistory(
        measurement: String,
        stationId: String? = null,
        start: String = "-1d",
        stop: String = "now()",
        interval: String = "1h"
    ): Map<String, List<SensorDataPoint>>

    fun getAllHistory(
        start: String = "-1d",
        stop: String = "now()",
        interval: String = "1h"
    ): Map<String, Map<String, List<SensorDataPoint>>>

    fun getStationHistory(
        stationId: String,
        start: String = "-1d",
        stop: String = "now()",
        interval: String = "1h"
    ): Map<String, Map<String, List<SensorDataPoint>>>
}