package com.weather.weatherapp.weatherForecast.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.weather.weatherapp.weatherForecast.ForecastType;
import com.weather.weatherapp.weatherForecast.customserilizer.RoundDoubleSerializer;

import java.time.LocalDateTime;
import java.util.List;

public record WeatherForecastResponseHourlyDTO(
        String city,
        List<String> time,
        @JsonProperty("temperature_2m")
        @JsonSerialize(contentUsing = RoundDoubleSerializer.class)
        List<Double> temperature,
        @JsonProperty("weather_code")
        List<String> apperiance,
        @JsonProperty("uv_index")
        @JsonSerialize(contentUsing = RoundDoubleSerializer.class)
        List<Double> uvIndex,
        @JsonProperty("relativehumidity_2m")
        List<Integer> relativeHumidity2m,
        @JsonProperty("apparent_temperature")
        @JsonSerialize(contentUsing = RoundDoubleSerializer.class)
        List<Double> apparentTemperature,
        @JsonProperty("windspeed_10m")
        @JsonSerialize(contentUsing = RoundDoubleSerializer.class)
        List<Double> windspeed10m,
        @JsonProperty("pressure_msl")
        @JsonSerialize(contentUsing = RoundDoubleSerializer.class)
        List<Double> pressureMsl,
        List<Integer> visibility
) {
}
