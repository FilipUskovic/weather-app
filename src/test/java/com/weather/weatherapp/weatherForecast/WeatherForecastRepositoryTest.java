package com.weather.weatherapp.weatherForecast;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WeatherForecastRepositoryTest {

    @Autowired
    private WeatherForecastRepository weatherForecastRepository;

    @Container
    static MySQLContainer<?> postgres = new MySQLContainer<>(
            DockerImageName.parse("mysql:latest").asCompatibleSubstituteFor("mysql")
    );

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }


    @Test
    void findByCityAndForecastTypeAndDateTimeBetween_shouldReturnCorrectForecasts() {
        // Given
        String city = "TestCity";
        ForecastType forecastType = ForecastType.HOURLY;
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(1);

        WeatherForecastEntity forecast1 = new WeatherForecastEntity();
        forecast1.setCity(city);
        forecast1.setForecastType(forecastType);
        forecast1.setDateTime(startDate.plusHours(1));
        weatherForecastRepository.save(forecast1);

        WeatherForecastEntity forecast2 = new WeatherForecastEntity();
        forecast2.setCity(city);
        forecast2.setForecastType(forecastType);
        forecast2.setDateTime(startDate.plusHours(2));
        weatherForecastRepository.save(forecast2);

        // When
        Optional<List<WeatherForecastEntity>> result = weatherForecastRepository.findByCityAndForecastTypeAndDateTimeBetween(city, forecastType, startDate, endDate);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(2);
        assertThat(result.get()).extracting(WeatherForecastEntity::getCity).containsOnly(city);
        assertThat(result.get()).extracting(WeatherForecastEntity::getForecastType).containsOnly(forecastType);
    }


}

