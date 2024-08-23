package com.weather.weatherapp.city.dto;

import jakarta.validation.constraints.NotBlank;

public record CityRequest(
        @NotBlank(message = "Ime grada je obavezno")
        String city
) {
}
