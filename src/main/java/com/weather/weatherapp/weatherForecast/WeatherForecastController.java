package com.weather.weatherapp.weatherForecast;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/weather")
public class WeatherForecastController {

    public final WeatherForecastService weatherForecastService;

    public WeatherForecastController(WeatherForecastService weatherForecastService) {
        this.weatherForecastService = weatherForecastService;
    }

    @GetMapping("home")
    public String hi (){
        return "hello";
    }

    @GetMapping("/current/{city}")
    public ResponseEntity<WeatherForecastEntity> getCurrentWeather(@PathVariable String city) {
        var WeatherForecastEntity = weatherForecastService.getWeather(city);
        return ResponseEntity.ok(WeatherForecastEntity);
    }




}
