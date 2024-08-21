package com.weather.weatherapp.weatherForecast.Predictions;

import com.weather.weatherapp.weatherForecast.Predictions.dto.TemperaturePrediction;
import com.weather.weatherapp.weatherForecast.Predictions.dto.TemperatureTrend;
import com.weather.weatherapp.weatherForecast.WeatherForecastRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class WeatherPredictionService {
    private final WeatherForecastRepository weatherForecastRepository;

    public WeatherPredictionService(WeatherForecastRepository weatherForecastRepository) {
        this.weatherForecastRepository = weatherForecastRepository;
    }

    public List<TemperaturePrediction> predictTemperature(String cityName, LocalDateTime startDate, int days) {
        List<TemperatureTrend> historicalData = weatherForecastRepository.getTemperatureTrend(
                cityName,
                startDate.minusDays(30),
                startDate
        );

        if (historicalData.size() < 7) {
            throw new IllegalStateException("Insufficient historical data for prediction");
        }

        double slope = calculateSlope(historicalData);
        double intercept = calculateIntercept(historicalData, slope);

        return IntStream.range(0, days)
                .mapToObj(i -> {
                    LocalDate date = startDate.toLocalDate().plusDays(i);
                    double predictedTemp = slope * i + intercept;
                    return new TemperaturePrediction(date, predictedTemp);
                })
                .collect(Collectors.toList());
    }

    private double calculateSlope(List<TemperatureTrend> data) {
        int n = data.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            double x = i;
            double y = data.get(i).averageTemperature();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        return (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
    }

    private double calculateIntercept(List<TemperatureTrend> data, double slope) {
        double sumY = data.stream().mapToDouble(TemperatureTrend::averageTemperature).sum();
        double sumX = IntStream.range(0, data.size()).sum();
        int n = data.size();

        return (sumY - slope * sumX) / n;
    }
}
