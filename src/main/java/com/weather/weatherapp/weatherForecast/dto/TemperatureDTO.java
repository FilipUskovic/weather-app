package com.weather.weatherapp.weatherForecast.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TemperatureDTO(float temp,
                             int humidity,
                             int pressure,
                             @JsonProperty("feels_like")
                             float feelsLike) {


}
