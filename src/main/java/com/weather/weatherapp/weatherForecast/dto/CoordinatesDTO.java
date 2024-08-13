package com.weather.weatherapp.weatherForecast.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CoordinatesDTO(
        @JsonProperty("lat")
        float lat,
        @JsonProperty("lon")
        float lon
) {
        // validacije
        public CoordinatesDTO {
                if (lat < -90 || lat > 90) {
                        throw new IllegalArgumentException("Latitude must be between -90 and 90");
                }
                if (lon < -180 || lon > 180) {
                        throw new IllegalArgumentException("Longitude must be between -180 and 180");
                }
        }
}
