package com.weather.weatherapp.weatherForecast;

import com.weather.weatherapp.city.CityEntity;
import com.weather.weatherapp.email.EmailService;
import com.weather.weatherapp.user.UserEntity;
import com.weather.weatherapp.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class WeatherAlertService {
    private static final Logger log = LoggerFactory.getLogger(WeatherAlertService.class);

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
                       .findTopByCityOrderByDateTimeDesc(city.getName())
                       .orElse(null);

               if (latestForecast != null) {
                    log.info("Latest forecast for {}: temp={}, humidity={}, windSpeed={}",
                           city.getName(), latestForecast.getTemperature(),
                           latestForecast.getHumidity(), latestForecast.getWindSpeed());

                   if (isSignificantChange(latestForecast)) {
                       log.info("Significant change detected for {}", city.getName());
                       sendWeatherNotification(user.getEmail(), user.getUsername(), city.getName(), latestForecast);
                   }
               } else {
                   log.warn("No forecast found for city: {}", city.getName());
               }
           }
       }
   }



        private boolean isSignificantChange (WeatherForecastEntity forecast){
            // Implementacija logike za određivanje značajne promjene
            // Na primjer, ako je temperatura iznad 30°C ili ispod 0°C
            return forecast.getTemperature() > 30 || forecast.getTemperature() < 0 ||
                    forecast.getWindSpeed() > 20 || forecast.getHumidity() > 90;
        }

    @Async
    public CompletableFuture<Void> sendWeatherNotification(String email, String username, String city, WeatherForecastEntity forecast) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Attempting to send email to: {} for city: {}", email, city);
                emailService.sendWeatherAlert(email, username, city, forecast);
                log.info("Email sent successfully to: {} for city: {}", email, city);
            } catch (Exception e) {
                log.error("Error sending email to {} for city {}: {}", email, city, e.getMessage());
                e.printStackTrace();
            }
        });
    }
}