package com.weather.weatherapp.weatherForecast.dto;

import com.weather.weatherapp.weatherForecast.ForecastType;
import com.weather.weatherapp.weatherForecast.WeatherForecastEntity;

import java.io.Serializable;
import java.time.LocalDateTime;

public record WeatherForecastCacheDTO(
        String city,
        double temperature,
        String description,
        LocalDateTime dateTime,
        int uvIndex,
        int visibility,
        int humidity,
        int pressure,
        double feelsLikeTemperature,
        double windSpeed,
        ForecastType forecastType
)implements Serializable {
    public static WeatherForecastCacheDTO fromEntity(WeatherForecastEntity entity) {
        return new WeatherForecastCacheDTO(
                entity.getCity(),
                entity.getTemperature(),
                entity.getDescription(),
                entity.getDateTime(),
                entity.getUvIndex(),
                entity.getVisibility(),
                entity.getHumidity(),
                entity.getPressure(),
                entity.getFeelsLikeTemperature(),
                entity.getWindSpeed(),
                entity.getForecastType()
        );
    }

    public WeatherForecastEntity toEntity() {
        WeatherForecastEntity entity = new WeatherForecastEntity();
        entity.setCity(city);
        entity.setTemperature((float) temperature);
        entity.setDescription(description);
        entity.setDateTime(dateTime);
        entity.setUvIndex(uvIndex);
        entity.setVisibility(visibility);
        entity.setHumidity(humidity);
        entity.setPressure(pressure);
        entity.setFeelsLikeTemperature((float) feelsLikeTemperature);
        entity.setWindSpeed((float) windSpeed);
        entity.setForecastType(forecastType);
        return entity;
    }
}
