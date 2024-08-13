package com.weather.weatherapp.weatherForecast;

import com.weather.weatherapp.weatherForecast.dto.UVResponseDTO;
import com.weather.weatherapp.weatherForecast.dto.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
public class WeatherForecastService {

    /* Spring preporucuje
       RestTemple nam omogucuje pozive prema vanjskim servisima u nasem slucaju WeatherMapApi-u
       Injekati si api key jer je dobra praka za sigurnosti, jer nam je api key konfiguriran izvan koda
       Dobra je praksa jer se lakse moze testirati kod korz konstrukor a i posot je ginal polje imutable je tj ne moze se projeniti
     */

    private final String apiKey;
    private final WeatherForecastRepository forecastRepository;
    private final RestTemplate restTemplate;

    public WeatherForecastService(WeatherForecastRepository weatherForecastRepository,
                                  RestTemplate restTemplate,
                                  @Value("${weather_api_key}") String apiKey) {
        this.forecastRepository = weatherForecastRepository;
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }


    public WeatherForecastEntity getWeather(String city) {
        // Dohvaca podatke o gradu pomocu openweathermap-a
        String weatherUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey;
        ResponseEntity<WeatherResponse> response = restTemplate.exchange(
                weatherUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        WeatherResponse weatherResponse = response.getBody();

        String uvUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + weatherResponse.cords().lat()
                + "&lon=" + weatherResponse.cords().lon() + "&appid=" + apiKey;

        ResponseEntity<UVResponseDTO> responseDTO = restTemplate.exchange(
                uvUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        UVResponseDTO uvResponseDTO = responseDTO.getBody();

        WeatherForecastEntity forecast = new WeatherForecastEntity();
        forecast.setCity(city);
        forecast.setTemperature(weatherResponse.temp().temp());
        forecast.setDescription(weatherResponse.descriptionDTO().getFirst().description());
        forecast.setDateTime(LocalDateTime.now());
        forecast.setUvIndex((int) uvResponseDTO.value());
        forecast.setVisibility(weatherResponse.visibility());

        return forecastRepository.save(forecast);
}
}
