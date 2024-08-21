package com.weather.weatherapp.weatherForecast.Predictions;

import com.weather.weatherapp.weatherForecast.Predictions.dto.TemperatureExtremes;
import com.weather.weatherapp.weatherForecast.Predictions.dto.TemperatureTrend;
import com.weather.weatherapp.weatherForecast.WeatherForecastRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WeatherAnalysisService {

    private final WeatherForecastRepository weatherForecastRepository;

    public WeatherAnalysisService(WeatherForecastRepository weatherForecastRepository) {
        this.weatherForecastRepository = weatherForecastRepository;
    }


    public List<TemperatureTrend> getTemperatureTrend(String cityName, LocalDateTime startDate, LocalDateTime endDate) {
        return weatherForecastRepository.getTemperatureTrend(cityName, startDate, endDate);
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

    public boolean hasSignificantTemperatureChange(String cityName, LocalDateTime startDate, LocalDateTime endDate, double threshold) {
        List<TemperatureTrend> trend = getTemperatureTrend(cityName, startDate, endDate);
        if (trend.size() < 2) return false;

        double firstTemp = trend.getFirst().averageTemperature();
        double lastTemp = trend.getLast().averageTemperature();
        return Math.abs(lastTemp - firstTemp) >= threshold;
    }
}
