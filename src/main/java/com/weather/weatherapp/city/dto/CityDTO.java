package com.weather.weatherapp.city.dto;

import java.util.List;
import java.util.Set;

public record CityDTO (
        Long id,
        String name,
        List<WeatherDTO> weatherForecasts,
        Set<Long>userIds
){
}
