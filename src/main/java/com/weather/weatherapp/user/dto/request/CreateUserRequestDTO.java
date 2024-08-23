package com.weather.weatherapp.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequestDTO(
        @NotBlank(message = "Korisničko ime je obavezno")
        @Size(min = 3, max = 50, message = "Korisničko ime mora biti između 3 i 50 znakova")
        String username,
        @NotBlank(message = "Email je obavezan")
        @Email(message = "Email mora biti validan")
        String email,
        @NotBlank(message = "Lozinka je obavezna")
        @Size(min = 6, message = "Lozinka mora imati najmanje 6 znakova")
        String password
) {
}
