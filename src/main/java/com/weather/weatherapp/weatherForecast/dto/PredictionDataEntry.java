package com.weather.weatherapp.weatherForecast.dto;

import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

@Embeddable
public record PredictionDataEntry(
        LocalDateTime dateTime,
        float predictedTemperature,
        Integer predictedHumidity,
        Float predictedWindSpeed
) {
}
