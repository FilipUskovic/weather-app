package com.weather.weatherapp.providers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenMeteoGoeCodingRes(
        @JsonProperty("results")
        List<GeosResult> resultL ,
        String name
) {
}
