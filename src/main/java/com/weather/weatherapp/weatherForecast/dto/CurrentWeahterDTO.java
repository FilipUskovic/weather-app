package com.weather.weatherapp.weatherForecast.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CurrentWeahterDTO(
        String time,
        @JsonProperty("temperature_2m")
        Double temperature2m,
        @JsonProperty("relativehumidity_2m")
        Integer relativeHumidity2m,
        @JsonProperty("apparent_temperature")
        Double apparentTemperature,
        @JsonProperty("windspeed_10m")
        Double windspeed10m,
        @JsonProperty("pressure_msl")
        Double pressureMsl,
        Integer visibility,
        @JsonProperty("uv_index")
        Double uvIndex
) {
}
