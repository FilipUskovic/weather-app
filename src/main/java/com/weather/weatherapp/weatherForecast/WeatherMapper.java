package com.weather.weatherapp.weatherForecast;

import com.weather.weatherapp.weatherForecast.dto.response.WeatherCastResponeDaily;
import com.weather.weatherapp.weatherForecast.dto.response.WeatherForecastResponseDTO;
import org.springframework.stereotype.Component;


@Component
public class WeatherMapper {

// TODO HISTORICAL DATA DODATI

    public WeatherForecastResponseDTO convertToDTO(WeatherForecastEntity entity) {
        return new WeatherForecastResponseDTO(
                entity.getCity(),
                entity.getDateTime(),
                entity.getTemperature(),
                entity.getDescription(),
                entity.getUvIndex(),
                entity.getVisibility(),
                entity.getHumidity(),
                entity.getWindSpeed(),
                entity.getFeelsLikeTemperature(),
                entity.getPressure(),
                entity.getForecastType()
        );
    }

    public WeatherCastResponeDaily convertToDTODaily(WeatherForecastEntity entity) {
        return new WeatherCastResponeDaily(
                entity.getCity(),
                entity.getDateTime(),
                entity.getMaxTemperature(),
                entity.getMinTemperature(),
                entity.getDescription(),
                entity.getUvIndex(),
                entity.getFeelsLikeTemperature(),
                entity.getHumidity(),
                entity.getWindSpeed(),
                entity.getForecastType()
        );
    }

}
