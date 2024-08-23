package com.weather.weatherapp.weatherForecast;

import com.weather.weatherapp.city.CityEntity;
import com.weather.weatherapp.city.CityService;
import com.weather.weatherapp.config.caching.CachingRedisService;
import com.weather.weatherapp.exception.WeatherServiceException;
import com.weather.weatherapp.providers.ProviderMapper;
import com.weather.weatherapp.providers.WeatherProviderServis;
import com.weather.weatherapp.providers.dto.Coordinates;
import com.weather.weatherapp.weatherForecast.dto.HistoricalDataEntry;
import com.weather.weatherapp.weatherForecast.dto.WeatherForecastCacheDTO;
import com.weather.weatherapp.weatherForecast.dto.WeatherResponse;
import com.weather.weatherapp.weatherForecast.dto.response.WeatherCastResponeDaily;
import com.weather.weatherapp.weatherForecast.dto.response.WeatherForecastResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WeatherDataService {
    private static final Logger log = LoggerFactory.getLogger(WeatherDataService.class);
    private final WeatherProviderServis weatherProvider;
    private final WeatherForecastRepository forecastRepository;
    private final CityService cityService;
    private final CachingRedisService cacheService;
    private final WeatherMapper mapper;
    private static final String CACHE_KEY_PREFIX = "weather:";
    private static final long CACHE_TTL = 3600; // 1 sat u sekundama

    public WeatherDataService(WeatherProviderServis weatherProvider,
                              WeatherForecastRepository forecastRepository,
                              CityService cityService,
                              CachingRedisService cacheService,
                              WeatherMapper mapper) {
        this.weatherProvider = weatherProvider;
        this.forecastRepository = forecastRepository;
        this.cityService = cityService;
        this.cacheService = cacheService;
        this.mapper = mapper;
    }
    public WeatherForecastResponseDTO getWeather(String city) {
        String cacheKey = CACHE_KEY_PREFIX + "current:" + city;
        Object cachedData = cacheService.getCachedData(cacheKey);
        if (cachedData instanceof WeatherForecastCacheDTO) {
            log.info("Vraćam podatke o vremenu iz cachea za {}", city);
            WeatherForecastEntity cachedForecast = ((WeatherForecastCacheDTO) cachedData).toEntity();
            return mapper.convertToDTO(cachedForecast);
        }

        CityEntity cityEntity = cityService.getOrCreateCity(city);
        WeatherForecastEntity forecast = fetchWeatherForCity(cityEntity);
        WeatherForecastCacheDTO cacheDTO = WeatherForecastCacheDTO.fromEntity(forecast);
        cacheService.cacheData(cacheKey, cacheDTO, CACHE_TTL);

        log.info("Spremljeni novi podaci o vremenu za getWeather {} u cache", city);
        return mapper.convertToDTO(forecastRepository.save(forecast));
    }

    public List<WeatherForecastResponseDTO> getHourly(String city) {
        String cacheKey = CACHE_KEY_PREFIX + "hourly:" + city;
        Object cachedData = cacheService.getCachedData(cacheKey);
        if (cachedData instanceof List<?> cachedList) {
            if (!cachedList.isEmpty() && cachedList.getFirst() instanceof WeatherForecastResponseDTO) {
                log.info("Vraćam satnu prognozu iz cachea za {}", city);
                return (List<WeatherForecastResponseDTO>) cachedList;
            }
        }

        CityEntity cityEntity = cityService.getOrCreateCity(city);
        List<WeatherForecastEntity> forecasts = weatherProvider.fetchAndSaveHourlyForecast(cityEntity);
        List<WeatherForecastResponseDTO> result = forecasts.stream()
                .map(mapper::convertToDTO)
                .collect(Collectors.toList());

        cacheService.cacheData(cacheKey, result, CACHE_TTL);
        return result;
    }

    public List<WeatherCastResponeDaily> getDaily(String city) {
        String cacheKey = CACHE_KEY_PREFIX + "daily:" + city;
        Object cachedData = cacheService.getCachedData(cacheKey);
        if (cachedData instanceof List<?> cachedList) {
            if (!cachedList.isEmpty() && cachedList.getFirst() instanceof WeatherCastResponeDaily) {
                log.info("Vraćam dnevnu prognozu iz cachea za {}", city);
                return (List<WeatherCastResponeDaily>) cachedList;
            }
        }

        CityEntity cityEntity = cityService.getOrCreateCity(city);
        List<WeatherForecastEntity> forecasts = weatherProvider.fetchAndSaveDailyForecast(cityEntity);
        List<WeatherCastResponeDaily> result = forecasts.stream()
                .map(mapper::convertToDTODaily)
                .collect(Collectors.toList());

        cacheService.cacheData(cacheKey, result, CACHE_TTL);
        return result;
    }

    private WeatherForecastEntity fetchWeatherForCity(CityEntity cityEntity) {
        try {
            WeatherResponse responseWeather = weatherProvider.fetchWeatherData(cityEntity);
            if (responseWeather == null) {
                throw new WeatherServiceException("Neuspjelo dohvaćanje podataka o vremenu za grad: " + cityEntity.getName());
            }
            float uvIndex = weatherProvider.fetchUVData(responseWeather.cords().lat(), responseWeather.cords().lng());
            WeatherForecastEntity forecast = ProviderMapper.toWeatherForecast(cityEntity, responseWeather, uvIndex);
            forecast.setForecastType(ForecastType.CURRENT);
            return forecast;
        } catch (Exception e) {
            log.warn("Neuspjelo dohvaćanje podataka s OpenWeatherMap za {}. Pokušavam s geolokacijom.", cityEntity.getName());
            try {
                Coordinates coords = weatherProvider.getCordinates(cityEntity);
                return weatherProvider.fetchOpenMeteoWeather(cityEntity, coords);
            } catch (Exception geoException) {
                log.error("Neuspjelo dohvaćanje vremena za {} korištenjem geolokacije", cityEntity.getName(), geoException);
                throw new RuntimeException("Neuspjelo dohvaćanje vremenske prognoze", geoException);
            }
        }
    }

    public HistoricalDataEntry getHistoricalData(String city, LocalDate startDate, LocalDate endDate) {
        String cacheKey = CACHE_KEY_PREFIX + "historical:" + city + ":" + startDate + ":" + endDate;
        Object cachedData = cacheService.getCachedData(cacheKey);
        if (cachedData instanceof HistoricalDataEntry) {
            log.info("Vraćam povijesne podatke iz cachea za grad: {} od {} do {}", city, startDate, endDate);
            return (HistoricalDataEntry) cachedData;
        }
        CityEntity cityEntity = cityService.getOrCreateCity(city);
        Coordinates coords = weatherProvider.getCordinates(cityEntity);
        HistoricalDataEntry historicalData = weatherProvider.fetchOpenMeteoHistoricalData(
                coords.latitude(), coords.longitude(), startDate, endDate);

        if (historicalData != null && historicalData.daily() != null) {
            List<WeatherForecastEntity> forecasts = ProviderMapper.toHistoricalWeather(cityEntity, historicalData);
            forecastRepository.saveAll(forecasts);
            cacheService.cacheData(cacheKey, historicalData, CACHE_TTL * 24); // Cache for 24 hours
        } else {
            log.warn("Dobiveni su null podaci za povijesne podatke za grad: {}", city);
        }
        return historicalData;
    }
}
