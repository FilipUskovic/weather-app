package com.weather.weatherapp.weatherForecast.Predictions;

import com.weather.weatherapp.weatherForecast.Predictions.dto.TemperaturePrediction;
import com.weather.weatherapp.weatherForecast.Predictions.dto.TemperatureTrend;
import com.weather.weatherapp.weatherForecast.WeatherForecastRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherPredictionServiceTest {

    @Mock
    private WeatherForecastRepository weatherForecastRepository;

    private WeatherPredictionService weatherPredictionService;

    @BeforeEach
    void setUp() {
        weatherPredictionService = new WeatherPredictionService(weatherForecastRepository);
    }

    @Test
    void predictTemperature_shouldThrowExceptionIfInsufficientData() {
        // Given
        String cityName = "TestCity";
        LocalDateTime startDate = LocalDateTime.now();
        int days = 7;
        when(weatherForecastRepository.getTemperatureTrend(any(), any(), any())).thenReturn(Arrays.asList(new TemperatureTrend(startDate.toLocalDate(), 25.0)));

        // When & Then
        assertThatThrownBy(() -> weatherPredictionService.predictTemperature(cityName, startDate, days))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient historical data for prediction");
    }

    @Test
    void predictTemperature_shouldReturnCorrectPredictions() {
        String cityName = "TestCity";
        LocalDateTime startDate = LocalDateTime.now();
        int days = 7;
        List<TemperatureTrend> historicalData = Arrays.asList(
                new TemperatureTrend(startDate.minusDays(7).toLocalDate(), 20.0),
                new TemperatureTrend(startDate.minusDays(6).toLocalDate(), 21.0),
                new TemperatureTrend(startDate.minusDays(5).toLocalDate(), 22.0),
                new TemperatureTrend(startDate.minusDays(4).toLocalDate(), 23.0),
                new TemperatureTrend(startDate.minusDays(3).toLocalDate(), 24.0),
                new TemperatureTrend(startDate.minusDays(2).toLocalDate(), 25.0),
                new TemperatureTrend(startDate.minusDays(1).toLocalDate(), 26.0)
        );
        when(weatherForecastRepository.getTemperatureTrend(any(), any(), any())).thenReturn(historicalData);

        List<TemperaturePrediction> predictions = weatherPredictionService.predictTemperature(cityName, startDate, days);

        assertThat(predictions).hasSize(days);
        assertThat(predictions.get(0).predictedTemperature()).isCloseTo(27.0, within(0.5));
        assertThat(predictions.get(6).predictedTemperature()).isCloseTo(33.0, within(0.5));
    }
}