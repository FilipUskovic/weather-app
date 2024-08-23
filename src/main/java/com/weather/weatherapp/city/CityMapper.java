package com.weather.weatherapp.city;

import com.weather.weatherapp.city.dto.CityDTO;
import com.weather.weatherapp.city.dto.CityRequest;
import com.weather.weatherapp.city.dto.WeatherDTO;
import com.weather.weatherapp.user.UserEntity;
import com.weather.weatherapp.weatherForecast.WeatherForecastEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CityMapper {

    protected CityDTO convertToDTO(CityEntity city) {
        return new CityDTO(
                city.getId(),
                city.getName(),
                city.getWeatherForecasts().stream()
                        .map(this::convertToWeatherForecastDTO)
                        .collect(Collectors.toList()),
                city.getUsers().stream()
                        .map(UserEntity::getId)
                        .collect(Collectors.toSet())
        );
    }

    public CityEntity convertToEntity(CityRequest request) {
        return new CityEntity(request.city());
    }

    protected WeatherDTO convertToWeatherForecastDTO(WeatherForecastEntity forecast) {
        return new WeatherDTO(
                forecast.getId(),
                forecast.getDescription()
        );
    }
}
