package com.weather.weatherapp.weatherForecast.Predictions.dto;

import java.time.LocalDate;

public record TemperaturePrediction(LocalDate date, double predictedTemperature) {
}
