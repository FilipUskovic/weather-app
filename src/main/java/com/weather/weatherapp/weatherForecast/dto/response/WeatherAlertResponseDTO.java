package com.weather.weatherapp.weatherForecast.dto.response;

import java.time.LocalDateTime;

public record WeatherAlertResponseDTO(
        String cityName,
        String alertType,
        String description,
        LocalDateTime timestamp
) {
}
