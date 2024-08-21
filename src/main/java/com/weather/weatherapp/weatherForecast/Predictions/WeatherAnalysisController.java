package com.weather.weatherapp.weatherForecast.Predictions;

import com.weather.weatherapp.weatherForecast.Predictions.dto.TemperatureExtremes;
import com.weather.weatherapp.weatherForecast.Predictions.dto.TemperatureTrend;
import com.weather.weatherapp.weatherForecast.Predictions.dto.WeatherExtreme;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/weather/analysis")
public class WeatherAnalysisController {

    private final WeatherAnalysisService weatherAnalysisService;

    public WeatherAnalysisController(WeatherAnalysisService weatherAnalysisService) {
        this.weatherAnalysisService = weatherAnalysisService;
    }


    @GetMapping("/trend")
    public ResponseEntity<List<TemperatureTrend>> getTemperatureTrend(
        @RequestParam String cityName,
        @RequestParam LocalDateTime startDate,
        @RequestParam LocalDateTime endDate) {
    return ResponseEntity.ok(weatherAnalysisService.getTemperatureTrend(cityName, startDate, endDate));
}

    @GetMapping("/extremes")
    public ResponseEntity<List<TemperatureExtremes>> getTemperatureExtremes(
            @RequestParam String cityName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(weatherAnalysisService.getTemperatureExtremes(cityName, startDate, endDate));
    }

    @GetMapping("/extremes-weather")
    public ResponseEntity<WeatherExtreme> getWeatherExtremes(
            @RequestParam String city,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(weatherAnalysisService.getWeatherExtremes(city, startDate, endDate));
    }

    @GetMapping("/monthly-averages")
    public ResponseEntity<Map<Integer, Double>> getMonthlyAverageTemperatures(@RequestParam String cityName) {
        return ResponseEntity.ok(weatherAnalysisService.getMonthlyAverageTemperatures(cityName));
    }

    @GetMapping("/significant-change")
    public ResponseEntity<Boolean> hasSignificantTemperatureChange(
            @RequestParam String cityName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam double threshold) {
        try {
            boolean hasSignificantChange = weatherAnalysisService.hasSignificantTemperatureChange(cityName, startDate, endDate, threshold);
            return ResponseEntity.ok(hasSignificantChange);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(false);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(false);
        }
    }
}
