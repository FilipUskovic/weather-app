package com.weather.weatherapp.weatherForecast.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherResponse(
        @JsonProperty("coord")
        CoordinatesDTO cords,
        @JsonProperty("weather")
        List<DescriptionDTO> descriptionDTO,
        @JsonProperty("main")
        TemperatureDTO temp,
        @JsonProperty("visibility")
        int visibility,
        @JsonProperty("wind")
        WindDTO wind
) {
}
