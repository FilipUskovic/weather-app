package com.weather.weatherapp.weatherForecast.Predictions;

import com.weather.weatherapp.config.caching.CachingRedisService;
import com.weather.weatherapp.weatherForecast.Predictions.dto.TemperatureExtremes;
import com.weather.weatherapp.weatherForecast.Predictions.dto.TemperatureTrend;
import com.weather.weatherapp.weatherForecast.Predictions.dto.WeatherExtreme;
import com.weather.weatherapp.weatherForecast.WeatherForecastRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WeatherAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(WeatherAnalysisService.class);
    private final WeatherForecastRepository weatherForecastRepository;
    private final CachingRedisService cacheService;
    private static final String CACHE_KEY_PREFIX = "analysis:";
    private static final long CACHE_TTL = 86400; // 24 sata u sek

    public WeatherAnalysisService(WeatherForecastRepository weatherForecastRepository, CachingRedisService cacheService) {
        this.weatherForecastRepository = weatherForecastRepository;
        this.cacheService = cacheService;
    }

    // TODO dodati logiku ako datum ne postoji i validaciju da se ne moze unjeti datum startPiije end date-a
    public List<TemperatureTrend> getTemperatureTrend(String cityName, LocalDateTime startDate, LocalDateTime endDate) {
        String cacheKey = CACHE_KEY_PREFIX + "trend:" + cityName  + ":" + startDate + ":" + endDate;
        List<TemperatureTrend> cachedTrend = (List<TemperatureTrend>) cacheService.getCachedData(cacheKey);

        if (cachedTrend != null) {
            log.info("Returning cached temperature trend for {}", cityName);
            return cachedTrend;
        }

        List<TemperatureTrend> trend = weatherForecastRepository.getTemperatureTrend(cityName, startDate, endDate);
        cacheService.cacheData(cacheKey, trend, CACHE_TTL);
        log.info("Cached new temperature trend for {}", cityName);
        return trend;
        //  return weatherForecastRepository.getTemperatureTrend(cityName, startDate, endDate);
    }

    public List<TemperatureExtremes> getTemperatureExtremes(String cityName, LocalDateTime startDate, LocalDateTime endDate) {
        return weatherForecastRepository.getTemperatureExtremes(cityName, startDate, endDate);
    }

    public Map<Integer, Double> getMonthlyAverageTemperatures(String cityName) {
        Map<Integer, Double> monthlyAverages = new HashMap<>();
        for (int month = 1; month <= 12; month++) {
            Double avgTemp = weatherForecastRepository.getAverageTemperatureForMonth(cityName, month);
            if (avgTemp != null) {
                monthlyAverages.put(month, avgTemp);
            }
        }
        return monthlyAverages;
    }

    public WeatherExtreme getWeatherExtremes(String city, LocalDateTime startDate, LocalDateTime endDate) {
        return weatherForecastRepository.getWeatherExtremes(city, startDate, endDate);
    }


    public boolean hasSignificantTemperatureChange(String cityName, LocalDateTime startDate, LocalDateTime endDate, double threshold) {
        List<TemperatureTrend> trend = getTemperatureTrend(cityName, startDate, endDate);
        if (trend.size() < 2) return false;

        double firstTemp = trend.getFirst().averageTemperature();
        double lastTemp = trend.getLast().averageTemperature();
        return Math.abs(lastTemp - firstTemp) >= threshold;
    }
}
