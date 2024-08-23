package com.weather.weatherapp.config.caching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@EnableScheduling
public class CachingConfig {

    private static final Logger log = LoggerFactory.getLogger(CachingConfig.class);

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "currentWeather", "hourlyForecast", "dailyForecast",
                "weatherData", "uvIndex", "coordinates","historicalWeather"
        );
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    @CacheEvict(value = "currentWeather", allEntries = true)
    public void clearCurrentWeatherCache() {
        log.info("Clearing current weather cache");
    }

    @Scheduled(fixedRate = 6, timeUnit = TimeUnit.HOURS)
    @CacheEvict(value = {"hourlyForecast", "dailyForecast"}, allEntries = true)
    public void clearForecastCaches() {
        log.info("Clearing forecast caches");
    }


    @Scheduled(fixedRate = 12, timeUnit = TimeUnit.HOURS)
    @CacheEvict(value = "weatherData", allEntries = true)
    public void clearWeatherDataCache() {
        log.info("Clearing weather data cache");
    }


    @Scheduled(fixedRate = 24, timeUnit = TimeUnit.HOURS)
    @CacheEvict(value = {"uvIndex", "coordinates"}, allEntries = true)
    public void clearUVAndCoordinatesCache() {
        log.info("Clearing UV index and coordinates cache");
    }

    @Scheduled(fixedRate = 24, timeUnit = TimeUnit.HOURS)
    @CacheEvict(value = "historicalWeather", allEntries = true)
    public void clearHistoricalWeatherCache() {
        log.info("Clearing historical weather cache");
    }
}

