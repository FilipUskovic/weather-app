package com.weather.weatherapp.weatherForecast;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WeatherForecastRepository extends JpaRepository<WeatherForecastEntity, Long> {

    List<WeatherForecastEntity> findByCity(String city);

    Optional<List<WeatherForecastEntity>> findByCityAndForecastTypeAndDateTimeBetween(
            String city,
            ForecastType forecastType,
            LocalDateTime startDate,
            LocalDateTime endDate);

    Optional<WeatherForecastEntity> findFirstByCityAndForecastTypeOrderByDateTimeDesc(String city, ForecastType forecastType);

    List<WeatherForecastEntity> findByCityInAndForecastType(List<String> cities, ForecastType forecastType);


    @Query("select weather from WeatherForecastEntity weather where weather.city =:city and weather.forecastType=:forecastType" +
            " and weather.dateTime >=:startDate order by weather.dateTime")
    List<WeatherForecastEntity> findForecastByTypeAndTimeRange(String city, ForecastType forecastType, LocalDateTime startDate);

    @Query("Select weather from WeatherForecastEntity weather where weather.city in :cities and weather.forecastType =:forecastType ")
    List<WeatherForecastEntity> findCurrentForecastsForCities(List<String> cities, ForecastType forecastType);

}
