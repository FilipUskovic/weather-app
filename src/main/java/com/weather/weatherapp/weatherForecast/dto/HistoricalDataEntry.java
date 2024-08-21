package com.weather.weatherapp.weatherForecast.dto;

import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;
@Embeddable
public record HistoricalDataEntry(
        LocalDateTime dateTime,
        float temperature,
        Integer humidity,
        Float windSpeed
) {
}
