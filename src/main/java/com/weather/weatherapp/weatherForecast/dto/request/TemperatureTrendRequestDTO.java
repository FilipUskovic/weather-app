package com.weather.weatherapp.weatherForecast.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TemperatureTrendRequestDTO(
        @NotBlank(message = "Ime grada je obavezno")
        String cityName,

        @NotNull(message = "Početni datum je obavezan")
        LocalDateTime startDate,

        @NotNull(message = "Završni datum je obavezan")
        LocalDateTime endDate
) {
}
