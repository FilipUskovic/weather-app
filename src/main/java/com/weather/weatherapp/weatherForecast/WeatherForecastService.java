package com.weather.weatherapp.weatherForecast;

import com.weather.weatherapp.weatherForecast.dto.UVResponseDTO;
import com.weather.weatherapp.weatherForecast.dto.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


@Service
public class WeatherForecastService {

    /* Spring preporucuje
       RestTemple nam omogucuje pozive prema vanjskim servisima u nasem slucaju WeatherMapApi-u
       Injekati si api key jer je dobra praka za sigurnosti, jer nam je api key konfiguriran izvan koda
       Dobra je praksa jer se lakse moze testirati kod korz konstrukor a i posot je ginal polje imutable je tj ne moze se projeniti
       Web Clinet koristimo jer je novi u prekorucuje ga srpign team
     */

    private final String apiKey;
    private final WeatherForecastRepository forecastRepository;
    private final RestTemplate restTemplate;
    private final String openUvKey;

    public WeatherForecastService(WeatherForecastRepository weatherForecastRepository,
                                  RestTemplate restTemplate,
                                  @Value("${weather_api_key}") String apiKey,
                                  @Value("${open_UV_index}") String openUvKey) {
        this.forecastRepository = weatherForecastRepository;
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.openUvKey = openUvKey;
    }


    public WeatherForecastEntity getWeather(String city) {
        // Dohvaca podatke o gradu pomocu openweathermap-a
        WeatherResponse responseWeather = fetchWeatherData(city);
        System.out.println("weather response " + responseWeather);
        float uvIndex = fetchUVData(responseWeather.cords().lat(), responseWeather.cords().lng());

        WeatherForecastEntity forecast = WeatherMapper.toWeatherForecast(city, responseWeather, uvIndex);
        return forecastRepository.save(forecast);

    }

    private WeatherResponse fetchWeatherData(String city) {
        String weatherBaseUrl = "https://api.openweathermap.org/data/2.5/weather?";
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(weatherBaseUrl)
                .queryParam("q", city)
                .queryParam("appid", apiKey);
        return restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.GET,
                null,
                WeatherResponse.class
                ).getBody();
    }

    private float fetchUVData(float lat, float lng) {
        String openUvIndex = "https://api.openuv.io/api/v1/uv";
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(openUvIndex)
                .queryParam("lat", lat)
                .queryParam("lng", lng);

        System.out.println("urii2 " + uriBuilder.toUriString());
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-access-token", openUvKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        UVResponseDTO uvResponse = restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.GET,
                entity,
                UVResponseDTO.class
        ).getBody();
        System.out.println("urrr " + uvResponse.result().uv());
        return uvResponse.result().uv();
    }
}
