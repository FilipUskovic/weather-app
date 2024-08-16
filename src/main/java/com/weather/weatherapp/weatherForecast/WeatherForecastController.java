package com.weather.weatherapp.weatherForecast;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/weather")
public class WeatherForecastController {

    public final WeatherForecastService weatherForecastService;

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
        System.out.println("foreCast " + foreCast);
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
    public ResponseEntity<Void> addFavoriteCity(@RequestParam String username, @RequestParam String city) {
        weatherForecastService.addFavoriteCity(username, city);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/favorites")
    public ResponseEntity<Void> removeFavoriteCity(@RequestParam String username, @RequestParam String city) {
        weatherForecastService.removeFavoriteCity(username, city);
        return ResponseEntity.ok().build();
    }

}
