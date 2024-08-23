package com.weather.weatherapp.weatherForecast;

import com.weather.weatherapp.city.CityEntity;
import com.weather.weatherapp.config.caching.CachingRedisService;
import com.weather.weatherapp.providers.WeatherProviderServis;
import com.weather.weatherapp.providers.dto.Coordinates;
import com.weather.weatherapp.weatherForecast.dto.HistoricalDataEntry;
import com.weather.weatherapp.weatherForecast.dto.WeatherResponse;
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
    private final WeatherProviderServis weatherProvider;
    private final CachingRedisService cachingRedisService;
    private static final String CACHE_KEY_PREFIX = "weather:";
    private static final String HISTORICAL_CACHE_PREFIX = "historicalWeather:";
    private static final long HISTORICAL_CACHE_TTL = 24 * 60 * 60;
    private static final long CACHE_TTL = 3600; //

    public WeatherCacheService(WeatherForecastService service, WeatherProviderServis weatherProvider, CachingRedisService cacheService, CachingRedisService cachingRedisService) {
        this.weatherService = service;
        this.weatherProvider = weatherProvider;
        this.cachingRedisService = cachingRedisService;
    }

    @Cacheable(value = "currentWeather", key = "#city",
            unless = "#result == null || #result.dateTime.isBefore(T(java.time.LocalDateTime).now().minusHours(1))")
    public WeatherForecastResponseDTO getCachedCurrentWeather(String city) {
        log.info("Dohvaćanje trenutnog vremena za grad: {}", city);
        return weatherService.getWeather(city);
    }

    @Cacheable(value = "hourlyForecast", key = "#city",
            unless = "#result == null || #result.get(0).dateTime.isBefore(T(java.time.LocalDateTime).now())")
    public List<WeatherForecastResponseDTO> getCachedHourlyForecast(String city) {
        log.info("Dohvaćanje satne prognoze za grad: {}", city);
        return weatherService.getHourly(city);
    }

    @Cacheable(value = "dailyForecast", key = "#city",
            unless = "#result == null || #result.get(0).dateTime.isBefore(T(java.time.LocalDateTime).now().withHour(0).withMinute(0))")
    public List<WeatherCastResponeDaily> getCachedDailyForecast(String city) {
        log.info("Dohvaćanje dnevne prognoze za grad: {}", city);
        return weatherService.getDaily(city);
    }

    @Cacheable(value = "weatherData", key = "#city", unless = "#result == null")
    public WeatherResponse getCachedWeatherData(CityEntity city) {
        log.info("Dohvaćanje podataka o vremenu za grad: {}", city.getName());
        return weatherProvider.fetchWeatherData(city);
    }

    @Cacheable(value = "uvIndex", key = "#lat + '-' + #lng", unless = "#result == null")
    public float getCachedUVData(float lat, float lng) {
        log.info("Dohvaćanje UV indeksa za koordinate: {}, {}", lat, lng);
        return weatherProvider.fetchUVData(lat, lng);
    }

    @Cacheable(value = "coordinates", key = "#city", unless = "#result == null")
    public Coordinates getCachedCoordinates(CityEntity city) {
        log.info("Dohvaćanje koordinata za grad: {}", city.getName());
        return weatherProvider.getCordinates(city);
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

    @CachePut(value = "weatherData", key = "#city")
    public WeatherResponse refreshWeatherData(CityEntity city) {
        WeatherResponse freshData = weatherProvider.fetchWeatherData(city);
        log.info("Osvježeni podaci o vremenu za {}", city.getName());
        return freshData;
    }

    @CachePut(value = "uvIndex", key = "#lat + '-' + #lng")
    public float refreshUVData(float lat, float lng) {
        float freshData = weatherProvider.fetchUVData(lat, lng);
        log.info("Osvježeni UV podaci za koordinate: {}, {}", lat, lng);
        return freshData;
    }

    @CachePut(value = "coordinates", key = "#city")
    public Coordinates refreshCoordinates(CityEntity city) {
        Coordinates freshData = weatherProvider.getCordinates(city);
        log.info("Osvježene koordinate za {}", city.getName());
        return freshData;
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
