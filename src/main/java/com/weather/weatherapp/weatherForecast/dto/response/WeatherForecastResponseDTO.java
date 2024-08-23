package com.weather.weatherapp.weatherForecast.dto.response;

import com.weather.weatherapp.weatherForecast.ForecastType;

import java.time.LocalDateTime;

public record WeatherForecastResponseDTO(
        String cityName,
        LocalDateTime dateTime,
        double temperature,
        String description,
        int uvIndex,
        int visibility,
        int humidity,
        double windSpeed,
        double feelsLike,
        int pressure,
        ForecastType forecastType
) {
}
