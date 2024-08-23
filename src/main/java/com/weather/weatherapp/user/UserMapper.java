package com.weather.weatherapp.user;

import com.weather.weatherapp.city.CityEntity;
import com.weather.weatherapp.user.dto.response.UserResponseDTO;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    protected UserResponseDTO convertToDTO(UserEntity user) {
        return new UserResponseDTO(
                user.getId(),
                user.getRealUsername(),
                user.getEmail(),
                user.getFavoriteCities().stream()
                        .map(CityEntity::getName)
                        .collect(Collectors.toList())
        );
    }
}
