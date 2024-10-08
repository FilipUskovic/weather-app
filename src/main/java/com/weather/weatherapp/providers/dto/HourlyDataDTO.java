package com.weather.weatherapp.providers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record HourlyDataDTO(
        String city,
        List<String> time,
        @JsonProperty("temperature_2m")
        List<Double> temperature2m,
        @JsonProperty("weather_code")
        List<String> apperiance,
        @JsonProperty("uv_index")
        List<Double> uvIndex,
        @JsonProperty("relativehumidity_2m")
        List<Integer> relativeHumidity2m,
        @JsonProperty("apparent_temperature")
        List<Double> apparentTemperature,
        @JsonProperty("windspeed_10m")
        List<Double> windspeed10m,
        @JsonProperty("pressure_msl")
        List<Double> pressureMsl,
        List<Integer> visibility

) {
}
