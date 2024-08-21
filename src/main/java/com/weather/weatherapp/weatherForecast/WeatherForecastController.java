package com.weather.weatherapp.weatherForecast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.weatherapp.auth.jtw.JwtService;
import com.weather.weatherapp.city.FavoriteCityRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weather")
public class WeatherForecastController {
    private static final Logger log = LoggerFactory.getLogger(WeatherForecastController.class);

    public final WeatherForecastService weatherForecastService;
    private ObjectMapper objectMapper;
    private final JwtService jwtService;
    private final WeatherAlertService weatherAlertService;

    public WeatherForecastController(WeatherForecastService weatherForecastService, JwtService jwtService, WeatherAlertService weatherAlertService) {
        this.weatherForecastService = weatherForecastService;
        this.jwtService = jwtService;
        this.weatherAlertService = weatherAlertService;
    }
   // radi ali imamo samo 50 poziva za free
    @GetMapping("/current/{city}")
    public ResponseEntity<WeatherForecastEntity> getCurrentWeather(@PathVariable String city) {
        var WeatherForecastEntity = weatherForecastService.getWeather(city);
        return ResponseEntity.ok(WeatherForecastEntity);
    }


    // 1. API treba evolvirati zasebno od database seme
    // 2. Trebamo sakriti neke podatke od korisnika
    // 3.  Trebamo dodatna polja ili modficiratu podatke za api

    // radi no dodati potake za min temo max temo itd
    @GetMapping("/hourly/{city}")
    public ResponseEntity<List<WeatherForecastEntity>> getHourlyForecast(@PathVariable String city) {
        List<WeatherForecastEntity> foreCast = weatherForecastService.getHourly(city);
        return foreCast.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(foreCast);
    }

    // radi maknuti podatke koje ne treba
    @GetMapping("/daily/{city}")
    public ResponseEntity<List<WeatherForecastEntity>> getDailyForecast(@PathVariable String city) {
        return ResponseEntity.ok(weatherForecastService.getDaily(city));
    }
    // radi no 50 poziva samo zbog limited plana
    @GetMapping("/favorites")
    public ResponseEntity<List<WeatherForecastEntity>> getFavoritesCitiesWeather(@RequestParam String username) {
        return ResponseEntity.ok(weatherForecastService.getFavoritesCitiesWeather(username));
    }
    // radi no samo 50 poziva tvo limitet plana
    @PostMapping("/favorites")
    public ResponseEntity<?> addFavoriteCity(@RequestBody FavoriteCityRequest request,
                                             @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("Received request to add favorite city: {}", request);
        try {
            String email = (authHeader != null) ? jwtService.extractEmail(authHeader) : "default@example.com"; // Default email ako header nije prisutan
            log.info("Received extracted email {} ", email);
            weatherForecastService.addFavoriteCity(request.username(), request.city(), email);
            log.info("Successfully added favorite city {} for user {}", request.city(), request.username());
            return ResponseEntity.ok("Favorite city added successfully");
        } catch (Exception e) {
            log.error("Error adding favorite city", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add favorite city: " + e.getMessage());
        }
    }

    @DeleteMapping("/favorites")
    public ResponseEntity<Void> removeFavoriteCity(@RequestParam String username, @RequestParam String city) {
        weatherForecastService.removeFavoriteCity(username, city);
        return ResponseEntity.ok().build();
    }

    // weather alert


    @PostMapping("/check-alerts")
    public ResponseEntity<String> manuallyCheckAlerts() {
        weatherAlertService.checkAndSendAlerts();
        return ResponseEntity.ok("Alert check initiated manually");
    }


}
