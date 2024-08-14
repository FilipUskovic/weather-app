package com.weather.weatherapp.weatherForecast.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CoordinatesDTO(
        @JsonProperty("lat")
        float lat,
        @JsonProperty("lon")
        float lng
) {
}
