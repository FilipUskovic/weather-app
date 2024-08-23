package com.weather.weatherapp.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AddFavoriteCityRequestDTO(
        @NotBlank(message = "Ime grada je obavezno")
        String cityName,
        @NotBlank(message= "Korisnik mora biti unusen")
        String username
) {
}
