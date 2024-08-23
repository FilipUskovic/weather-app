package com.weather.weatherapp.weatherForecast;

import com.weather.weatherapp.city.CityEntity;
import com.weather.weatherapp.email.EmailService;
import com.weather.weatherapp.user.UserEntity;
import com.weather.weatherapp.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

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
       try {
           List<UserEntity> users = userRepository.findAll();
           if (users.isEmpty()) {
               log.info("Nema korisnika za provjeru upozorenja.");
               return;
           }

           List<String> allFavoriteCities = users.stream()
                   .flatMap(user -> user.getFavoriteCities().stream())
                   .map(CityEntity::getName)
                   .distinct()
                   .toList();

           if (allFavoriteCities.isEmpty()) {
               log.info("Nema omiljenih gradova za provjeru.");
               return;
           }

           // Dohvaćanje najnovijih prognoza za sve omiljene gradove
           Map<String, WeatherForecastEntity> forecastMap = allFavoriteCities.stream()
                   .map(city -> weatherForecastRepository.findTopByCityOrderByDateTimeDesc(city))
                   .flatMap(Optional::stream)
                   .collect(Collectors.toMap(WeatherForecastEntity::getCity, Function.identity()));

           if (forecastMap.isEmpty()) {
               log.warn("Nije pronađena nijedna prognoza za omiljene gradove.");
               return;
           }

           users.parallelStream().forEach(user -> processUserAlerts(user, forecastMap));
       } catch (Exception e) {
           log.error("Greška tijekom provjere i slanja upozorenja: {}", e.getMessage(), e);
       }
   }



    private void processUserAlerts(UserEntity user, Map<String, WeatherForecastEntity> forecastMap) {
        user.getFavoriteCities().stream()
                .map(CityEntity::getName)
                .map(forecastMap::get)
                .filter(Objects::nonNull)
                .filter(this::isSignificantChange)
                .forEach(forecast -> sendWeatherNotification(user.getEmail(), user.getUsername(), forecast));
    }

    private boolean isSignificantChange(WeatherForecastEntity forecast) {
        return forecast.getTemperature() > 30 || forecast.getTemperature() < 0 ||
                forecast.getWindSpeed() > 20 || forecast.getHumidity() > 90;
    }

    @Async
    public CompletableFuture<Void> sendWeatherNotification(String email, String username, WeatherForecastEntity forecast) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Slanje e-maila na: {} za grad: {}", email, forecast.getCity());
                emailService.sendWeatherAlert(email, username, forecast.getCity(), forecast);
                log.info("E-mail uspješno poslan na: {} za grad: {}", email, forecast.getCity());
            } catch (Exception e) {
                log.error("Greška prilikom slanja e-maila na {} za grad {}: {}", email, forecast.getCity(), e.getMessage(), e);
            }
        });
    }


    /* radii
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
     */
}