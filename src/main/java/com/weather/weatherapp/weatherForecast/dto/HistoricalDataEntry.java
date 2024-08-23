package com.weather.weatherapp.weatherForecast.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;
import java.util.List;

@Embeddable
public record HistoricalDataEntry(
        Daily daily
) {
        @Embeddable
        public record Daily(
                List<String> time,
                @JsonProperty("temperature_2m_max")
                List<Double> temperatureMax,
                @JsonProperty("temperature_2m_min")
                List<Double> temperatureMin,
                @JsonProperty("apparent_temperature_max")
                List<Double> feelsLikeMax,
                @JsonProperty("weather_code")
                List<Integer> weatherCode,
                @JsonProperty("uv_index_max")
                List<Double> uvIndexMax,
                @JsonProperty("wind_speed_10m_max")
                List<Double> windSpeedMax
        ) {}
}
