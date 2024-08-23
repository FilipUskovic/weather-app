package com.weather.weatherapp.weatherForecast;

import com.weather.weatherapp.city.CityEntity;
import com.weather.weatherapp.email.EmailService;
import com.weather.weatherapp.user.UserEntity;
import com.weather.weatherapp.user.UserRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherAlertServiceTest {

    @Mock
    private WeatherForecastRepository weatherForecastRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailService emailService;

    private WeatherAlertService weatherAlertService;

    @BeforeEach
    void setUp() {
        weatherAlertService = new WeatherAlertService(weatherForecastRepository, userRepository, emailService);
    }

    @Test
    void checkAndSendAlerts_shouldSendAlertsForSignificantChanges() throws MessagingException {
        // Given
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        CityEntity city = new CityEntity();
        city.setName("TestCity");
        user.setFavoriteCities(Collections.singleton(city));

        WeatherForecastEntity forecast = new WeatherForecastEntity();
        forecast.setCity("TestCity");
        forecast.setTemperature(35.0f);

        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));
        when(weatherForecastRepository.findTopByCityOrderByDateTimeDesc("TestCity")).thenReturn(Optional.of(forecast));

        // When
        weatherAlertService.checkAndSendAlerts();

        // Then
        verify(emailService, times(1)).sendWeatherAlert(
                eq("test@example.com"),
                eq("testuser"),
                eq("TestCity"),
                argThat(entity -> entity.getTemperature() == 35.0f && entity.getCity().equals("TestCity"))
        );
    }
}