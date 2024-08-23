package com.weather.weatherapp.providers.dto;

import java.time.LocalDate;

public record DailyTemperatureDTO(
        LocalDate date,
        double averageTemperature
) {
}
