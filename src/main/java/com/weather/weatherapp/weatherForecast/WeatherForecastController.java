package com.weather.weatherapp.weatherForecast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.weatherapp.city.FavoriteCityRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/weather")
public class WeatherForecastController {
    private static final Logger log = LoggerFactory.getLogger(WeatherForecastController.class);

    public final WeatherForecastService weatherForecastService;
    private ObjectMapper objectMapper;

    public WeatherForecastController(WeatherForecastService weatherForecastService) {
        this.weatherForecastService = weatherForecastService;
    }



    @GetMapping("/current/{city}")
    public ResponseEntity<WeatherForecastEntity> getCurrentWeather(@PathVariable String city) {
        var WeatherForecastEntity = weatherForecastService.getWeather(city);
        return ResponseEntity.ok(WeatherForecastEntity);
    }


    // 1. API treba evolvirati zasebno od database seme
    // 2. Trebamo sakriti neke podatke od korisnika
    // 3.  Trebamo dodatna polja ili modficiratu podatke za api
    @GetMapping("/hourly/{city}")
    public ResponseEntity<List<WeatherForecastEntity>> getHourlyForecast(@PathVariable String city) {
        List<WeatherForecastEntity> foreCast = weatherForecastService.getHourly(city);
        return foreCast.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(foreCast);
    }

    @GetMapping("/daily/{city}")
    public ResponseEntity<List<WeatherForecastEntity>> getDailyForecast(@PathVariable String city) {
        return ResponseEntity.ok(weatherForecastService.getDaily(city));
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<WeatherForecastEntity>> getFavoritesCitiesWeather(@RequestParam String username) {
        return ResponseEntity.ok(weatherForecastService.getFavoritesCitiesWeather(username));
    }

    @PostMapping("/favorites")
    public ResponseEntity<?> addFavoriteCity(@RequestBody FavoriteCityRequest request) {
        log.info("Received request to add favorite city: {}", request);
        try {
            weatherForecastService.addFavoriteCity(request.username(), request.city());
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

}
