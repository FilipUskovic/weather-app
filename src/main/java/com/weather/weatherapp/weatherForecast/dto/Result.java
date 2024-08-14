package com.weather.weatherapp.weatherForecast.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Result(
        @JsonProperty("uv")
        float uv
) {
}
