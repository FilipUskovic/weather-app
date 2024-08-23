package com.weather.weatherapp.providers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TemperatureDTO(float temp,
                             int humidity,
                             int pressure,
                             @JsonProperty("feels_like")
                             float feelsLike) {


}
