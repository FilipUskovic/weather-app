package com.weather.weatherapp.weatherForecast.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UVResponseDTO(
        @JsonProperty("result")
        Result result) {
}
