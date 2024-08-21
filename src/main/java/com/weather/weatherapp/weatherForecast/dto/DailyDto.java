package com.weather.weatherapp.weatherForecast.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DailyDto(
        List<String> time,
        @JsonProperty("temperature_2m_max")
        List<Double> temperatureMax,
        @JsonProperty("temperature_2m_min")
        List<Double> temperatureMin,
        @JsonProperty("weather_code")
        List<String> description,
        @JsonProperty("apparent_temperature_max")
        List<Double> apparentTemperatureMax,
        @JsonProperty("windspeed_10m_max")
        List<Double> windspeedMax,
        @JsonProperty("uv_index_max")
        List<Double> uvIndexMax,
        @JsonProperty("relative_humidity_2m_max")
        List<Double> relativeHumidityMax

) {
}
