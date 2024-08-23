package com.weather.weatherapp.weatherForecast.dto.response;
import com.weather.weatherapp.weatherForecast.ForecastType;

import java.time.LocalDateTime;

public record WeatherCastResponeDaily(
        String cityName,
        LocalDateTime dateTime,
        double temperatureMax,
        double temperatureMin,
        String description,
        int uvIndex,
        double feelsLikeMax,
        int humidity,
        double windSpeed,
        ForecastType forecastType
) {
}
