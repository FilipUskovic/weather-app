package com.weather.weatherapp.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@EnableScheduling
public class CachingConfig {

    @Bean
    public CacheManager cacheManager(){
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                new ConcurrentMapCache("currentWeather"),
                new ConcurrentMapCache("hourlyForecast"),
                new ConcurrentMapCache("dailyForecast"),
                new ConcurrentMapCache("weatherData"),
                new ConcurrentMapCache("uvIndex"),
                new ConcurrentMapCache("coordinates")
        ));
        return cacheManager;
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    @CacheEvict(value = "currentWeather", allEntries = true)
    public void clearCurrentWeatherCache() {
        System.out.println("Clearing current weather cache");
    }

    @Scheduled(fixedRate = 6, timeUnit = TimeUnit.HOURS)
    @CacheEvict(value = {"hourlyForecast", "dailyForecast"}, allEntries = true)
    public void clearForecastCaches() {
        System.out.println("Clearing forecast caches");
    }


}

