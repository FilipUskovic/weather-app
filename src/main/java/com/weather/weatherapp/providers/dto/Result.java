package com.weather.weatherapp.providers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Result(
        @JsonProperty("uv")
        float uv
) {
}
