package com.weather.weatherapp.user.dto.response;

import java.util.List;

public record UserResponseDTO(
        Long id,
        String username,
        String email,
        List<String> favoriteCities
) {
}
