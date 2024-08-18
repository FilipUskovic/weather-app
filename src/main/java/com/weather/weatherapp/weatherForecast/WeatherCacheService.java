package com.weather.weatherapp.weatherForecast;

import com.weather.weatherapp.city.CityEntity;
import com.weather.weatherapp.weatherForecast.dto.Coordinates;
import com.weather.weatherapp.weatherForecast.dto.WeatherResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WeatherCacheService {

    private final WeatherForecastService weatherService;

    public WeatherCacheService(WeatherForecastService service) {
        this.weatherService = service;
    }


    @Cacheable(value = "currentWeather", key = "#city",
            unless = "#result == null || #result.dateTime.isBefore(T(java.time.LocalDateTime).now().minusHours(1))")
    public WeatherForecastEntity getCachedCurrentWeather(String city) {
        return weatherService.getWeather(city);
    }

    @Cacheable(value = "hourlyForecast", key = "#city",
            unless = "#result == null || #result.get(0).dateTime.isBefore(T(java.time.LocalDateTime).now())")
    public List<WeatherForecastEntity> getCachedHourlyForecast(String city) {
        return weatherService.getHourly(city);
    }

    @Cacheable(value = "dailyForecast", key = "#city",
            unless = "#result == null || #result.get(0).dateTime.isBefore(T(java.time.LocalDateTime).now().withHour(0).withMinute(0))")
    public List<WeatherForecastEntity> getCachedDailyForecast(String city) {
        return weatherService.getDaily(city);
    }

    @Cacheable(value = "weatherData", key = "#city", unless = "#result == null")
    public WeatherResponse getCachedWeatherData(CityEntity city) {
        return weatherService.fetchWeatherData(city);
    }

    @Cacheable(value = "uvIndex", key = "#lat + '-' + #lng", unless = "#result == null")
    public float getCachedUVData(float lat, float lng) {
        return weatherService.fetchUVData(lat, lng);
    }

    @Cacheable(value = "coordinates", key = "#city", unless = "#result == null")
    public Coordinates getCachedCoordinates(CityEntity city) {
        return weatherService.getCordinates(city);
    }


    @CachePut(value = "hourlyForecast", key = "#city")
    public List<WeatherForecastEntity> refreshHourlyForecast(String city) {
        List<WeatherForecastEntity> freshData = weatherService.getHourly(city);
        System.out.println("Refreshed hourly forecast for " + city);
        return freshData;
    }

    @CachePut(value = "dailyForecast", key = "#city")
    public List<WeatherForecastEntity> refreshDailyForecast(String city) {
        List<WeatherForecastEntity> freshData = weatherService.getDaily(city);
        System.out.println("Refreshed daily forecast for " + city);
        return freshData;
    }


    // Metoda za čišćenje svih cache-ova
    @CacheEvict(value = {"currentWeather", "hourlyForecast", "dailyForecast", "weatherData", "uvIndex", "coordinates"}, allEntries = true)
    public void clearAllCaches() {
        System.out.println("Cleared all weather caches");
    }


    // Dodatne metode za selektivno osvježavanje drugih cache-ova
    @CachePut(value = "weatherData", key = "#city")
    public WeatherResponse refreshWeatherData(CityEntity city) {
        WeatherResponse freshData = weatherService.fetchWeatherData(city);
        System.out.println("Refreshed weather data for " + city);
        return freshData;
    }

    @CachePut(value = "uvIndex", key = "#lat + '-' + #lng")
    public float refreshUVData(float lat, float lng) {
        float freshData = weatherService.fetchUVData(lat, lng);
        System.out.println("Refreshed UV data for coordinates: " + lat + ", " + lng);
        return freshData;
    }

    @CachePut(value = "coordinates", key = "#city")
    public Coordinates refreshCoordinates(CityEntity city) {
        Coordinates freshData = weatherService.getCordinates(city);
        System.out.println("Refreshed coordinates for " + city);
        return freshData;
    }

}
