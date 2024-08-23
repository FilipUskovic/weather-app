package com.weather.weatherapp.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "polje ne moze biti prazno")
        @Email(message = " mora biti email @ ")
        String email,
        @NotBlank()
        @Size(message = "Mora biti minimalno 6 znakova", min = 6)
        String password
) {
}
