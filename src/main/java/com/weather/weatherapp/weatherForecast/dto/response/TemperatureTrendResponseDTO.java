package com.weather.weatherapp.weatherForecast.dto.response;

import com.weather.weatherapp.providers.dto.DailyTemperatureDTO;

import java.util.List;

public record TemperatureTrendResponseDTO(
        String cityName,
        List<DailyTemperatureDTO> dailyTemperatures
) {
}
