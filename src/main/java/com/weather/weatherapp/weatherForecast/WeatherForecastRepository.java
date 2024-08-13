package com.weather.weatherapp.weatherForecast;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeatherForecastRepository extends JpaRepository<WeatherForecastEntity, Long> {

    List<WeatherForecastEntity> findByCity(String city);
}
