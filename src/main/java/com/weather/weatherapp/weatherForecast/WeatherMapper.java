package com.weather.weatherapp.weatherForecast;


import com.weather.weatherapp.weatherForecast.dto.WeatherResponse;

import java.time.LocalDateTime;

public class WeatherMapper {

    public static WeatherForecastEntity toWeatherForecast(String city, WeatherResponse weather, float uvIndex) {
        return new WeatherForecastEntity(
                city,
                weather.temp().temp(),
                weather.descriptionDTO().getFirst().description(),
                LocalDateTime.now(),
                (int) uvIndex,
                weather.visibility()
        );
    }

}
