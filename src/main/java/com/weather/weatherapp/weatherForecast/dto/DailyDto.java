package com.weather.weatherapp.weatherForecast.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DailyDto(
        List<String> time,
        @JsonProperty("temperature_2m_max")
        List<Double> temperatureMax,
     //   @JsonProperty("temperature_2m_min")
     //   List<Double> temperatureMin,
        @JsonProperty("apparent_temperature_max")
        List<Double> apparentTemperatureMax,
   //     @JsonProperty("apparent_temperature_min")
    //    List<Double> apparentTemperatureMin,
      //  @JsonProperty("precipitation_sum")
       // List<Double> precipitationSum,
        @JsonProperty("windspeed_10m_max")
        List<Double> windspeedMax,
        @JsonProperty("uv_index_max")
        List<Double> uvIndexMax
       // @JsonProperty("pressure_msl")
        //List<Double> pressure
) {
}
