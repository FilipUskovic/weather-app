package com.weather.weatherapp.providers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UVResponseDTO(
        @JsonProperty("result")
        Result result) {
}
