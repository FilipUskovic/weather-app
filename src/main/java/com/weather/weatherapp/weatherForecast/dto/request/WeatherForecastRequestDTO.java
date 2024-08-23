package com.weather.weatherapp.weatherForecast.dto.request;

import com.weather.weatherapp.weatherForecast.ForecastType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WeatherForecastRequestDTO(
        @NotBlank(message = "Ime grada je obavezno")
        String cityName,

        @NotNull(message = "Tip prognoze je obavezan")
        ForecastType forecastType
) {
}
