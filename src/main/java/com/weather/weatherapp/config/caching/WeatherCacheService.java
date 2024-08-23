package com.weather.weatherapp.config.caching;

import com.weather.weatherapp.weatherForecast.WeatherForecastService;
import com.weather.weatherapp.weatherForecast.dto.HistoricalDataEntry;
import com.weather.weatherapp.weatherForecast.dto.response.WeatherCastResponeDaily;
import com.weather.weatherapp.weatherForecast.dto.response.WeatherForecastResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WeatherCacheService {

    private static final Logger log = LoggerFactory.getLogger(WeatherCacheService.class);
    private final WeatherForecastService weatherService;
    private final CachingRedisService cachingRedisService;
    private static final String CACHE_KEY_PREFIX = "weather:";
    private static final String HISTORICAL_CACHE_PREFIX = "historicalWeather:";
    private static final long HISTORICAL_CACHE_TTL = 24 * 60 * 60;

    public WeatherCacheService(WeatherForecastService service, CachingRedisService cachingRedisService) {
        this.weatherService = service;
        this.cachingRedisService = cachingRedisService;
    }

    @CachePut(value = "hourlyForecast", key = "#city")
    public List<WeatherForecastResponseDTO> refreshHourlyForecast(String city) {
        List<WeatherForecastResponseDTO> freshData = weatherService.getHourly(city);
        log.info("Osvježena satna prognoza za {}", city);
        return freshData;
    }

    @CachePut(value = "dailyForecast", key = "#city")
    public List<WeatherCastResponeDaily> refreshDailyForecast(String city) {
        List<WeatherCastResponeDaily> freshData = weatherService.getDaily(city);
        log.info("Osvježena dnevna prognoza za {}", city);
        return freshData;
    }

    @CacheEvict(value = {"currentWeather", "hourlyForecast", "dailyForecast", "weatherData", "uvIndex", "coordinates"}, allEntries = true)
    public void clearAllCaches() {
        log.info("Očišćeni svi weatherovi cachevi");
    }

    public void clearCacheForCity(String city) {
        cachingRedisService.deleteCachedData(CACHE_KEY_PREFIX + city);
        log.info("Obrisan cache za grad: {}", city);
    }


    @Cacheable(value = "historicalWeather", key = "#cacheKey", unless = "#result == null")
    public HistoricalDataEntry getCachedHistoricalData(String cacheKey) {
        String fullCacheKey = HISTORICAL_CACHE_PREFIX + cacheKey;
        Object cachedData = cachingRedisService.getCachedData(fullCacheKey);

        if (cachedData instanceof HistoricalDataEntry) {
            log.info("Dohvaćeni povijesni podaci iz cachea za ključ: {}", cacheKey);
            return (HistoricalDataEntry) cachedData;
        } else {
            log.info("Nema povijesnih podataka u cacheu za ključ: {}", cacheKey);
            return null;
        }
    }

    @CachePut(value = "historicalWeather", key = "#cacheKey")
    public HistoricalDataEntry cacheHistoricalData(String cacheKey, HistoricalDataEntry data) {
        String fullCacheKey = HISTORICAL_CACHE_PREFIX + cacheKey;
        cachingRedisService.cacheData(fullCacheKey, data, HISTORICAL_CACHE_TTL);
        log.info("Spremljeni povijesni podaci u cache za ključ: {}", cacheKey);
        return data;
    }

    public void clearHistoricalCache() {
        cachingRedisService.deleteAllCachedData(HISTORICAL_CACHE_PREFIX);
        log.info("Očišćen cijeli cache povijesnih podataka o vremenu");
    }

    public void clearHistoricalCacheForCity(String city) {
        String pattern = HISTORICAL_CACHE_PREFIX + city + "*";
        cachingRedisService.deleteAllCachedData(pattern);
        log.info("Očišćen cache povijesnih podataka za grad: {}", city);
    }
}
