package com.weather.weatherapp.weatherForecast.Predictions.dto;

import java.time.LocalDate;

public record TemperatureTrend(LocalDate date, double averageTemperature) {
    public TemperatureTrend(java.sql.Date date, double averageTemperature) {
        this(date.toLocalDate(), averageTemperature);
    }
}
