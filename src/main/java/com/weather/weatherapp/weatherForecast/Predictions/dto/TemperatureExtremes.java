package com.weather.weatherapp.weatherForecast.Predictions.dto;

import java.time.LocalDate;

public record TemperatureExtremes(LocalDate date, double minTemperature, double maxTemperature) {
    public TemperatureExtremes(java.sql.Date date, double minTemperature, double maxTemperature) {
        this(date.toLocalDate(), minTemperature, maxTemperature);
    }
}
