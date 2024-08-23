package com.weather.weatherapp.providers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CoordinatesDTO(
        @JsonProperty("lat")
        float lat,
        @JsonProperty("lon")
        float lng
) {
}
