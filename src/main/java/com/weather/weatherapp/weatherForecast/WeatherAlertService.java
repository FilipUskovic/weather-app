package com.weather.weatherapp.weatherForecast;

import com.weather.weatherapp.city.CityEntity;
import com.weather.weatherapp.email.EmailService;
import com.weather.weatherapp.user.UserEntity;
import com.weather.weatherapp.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WeatherAlertService {

    private final WeatherForecastRepository weatherForecastRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public WeatherAlertService(WeatherForecastRepository weatherForecastRepository, UserRepository userRepository, EmailService emailService) {
        this.weatherForecastRepository = weatherForecastRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

   // @Scheduled(cron = "0 0 * * * *") // Svaki sat
    public void checkAndSendAlerts() {
        List<UserEntity> users = userRepository.findAll();
        for (UserEntity user : users) {
            for (CityEntity city : user.getFavoriteCities()) {
                WeatherForecastEntity latestForecast = weatherForecastRepository
                        .findTopByCityOrderByDateTimeDesc(city)
                        .orElse(null);

                if (latestForecast != null) {
                    if (isSignificantChange(latestForecast)) {
                        sendAlert(user, city, latestForecast);
                    }
                }
            }
        }
    }



        private boolean isSignificantChange (WeatherForecastEntity forecast){
            // Implementacija logike za određivanje značajne promjene
            // Na primjer, ako je temperatura iznad 30°C ili ispod 0°C
            return forecast.getTemperature() > 30 || forecast.getTemperature() < 0;
        }

    private void sendAlert(UserEntity user, CityEntity city, WeatherForecastEntity forecast) {
        String subject = "Weather Alert for " + city.getName();
        String message = String.format("""
                        Dear %s,
                        There is a significant weather change in %s. \n
                        The current temperature is %.1f°C.
                        Best regards,
                        Weather App Team""",
                user.getUsername(), city.getName(), forecast.getTemperature());
        emailService.sendSimpleMessage(user.getEmail(), subject, message);
    }
}