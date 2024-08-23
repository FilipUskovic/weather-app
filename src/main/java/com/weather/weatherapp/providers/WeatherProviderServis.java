package com.weather.weatherapp.providers;

import com.weather.weatherapp.city.CityEntity;
import com.weather.weatherapp.city.CityService;
import com.weather.weatherapp.providers.dto.*;
import com.weather.weatherapp.weatherForecast.*;
import com.weather.weatherapp.weatherForecast.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class WeatherProviderServis {
    private static final Logger log = LoggerFactory.getLogger(WeatherProviderServis.class);

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String openUvKey;
    private final WeatherForecastRepository forecastRepository;
    private final TransactionTemplate transactionTemplate;



    public WeatherProviderServis(CityService cityService,
                                 RestTemplate restTemplate,
                                 @Value("${weather_api_key}") String apiKey,
                                 @Value("${open_UV_index}") String openUvKey,
                                 WeatherForecastRepository forecastRepository, TransactionTemplate transactionTemplate) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.openUvKey = openUvKey;
        this.forecastRepository = forecastRepository;
        this.transactionTemplate = transactionTemplate;

    }


    public HistoricalDataEntry fetchOpenMeteoHistoricalData(float lat, float lon, LocalDate startDate, LocalDate endDate) {
        String openMeteoUrl = "https://archive-api.open-meteo.com/v1/archive";
        String url = UriComponentsBuilder.fromHttpUrl(openMeteoUrl)
                .queryParam("latitude", lat)
                .queryParam("longitude", lon)
                .queryParam("start_date", startDate.toString())
                .queryParam("end_date", endDate.toString())
                .queryParam("daily", "weather_code,temperature_2m_max,temperature_2m_min,apparent_temperature_max,uv_index_max,wind_speed_10m_max")
                .toUriString();

        log.info("Zahtjev OpenMeteo za povijesne podatke s URL-om: {}", url);

        HistoricalDataEntry response = restTemplate.getForObject(url, HistoricalDataEntry.class);

        if (response == null) {
            throw new RuntimeException("Neuspjelo dohvaćanje povijesnih podataka s OpenMeteo");
        }
        log.info("Primljen OpenMeteo odgovor za povijesne podatke: {}", response);
        return response;
    }


    public OpenMeteResponse fetchOpenMeteoDailyData(float lat, float lon) {
        String openMeteoUrl = "https://api.open-meteo.com/v1/forecast?";
        String url = UriComponentsBuilder.fromHttpUrl(openMeteoUrl)
                .queryParam("latitude", lat)
                .queryParam("longitude", lon)
                .queryParam("daily", "temperature_2m_max,temperature_2m_min,weather_code,apparent_temperature_max,relative_humidity_2m_max,windspeed_10m_max,uv_index_max")
                .queryParam("forecast_days", 7)
                .toUriString();
        log.info("Zahtjev OpenMeteo za 7-dnevne podatke s URL-om: {}", url);
        OpenMeteResponse response = Optional.ofNullable(restTemplate.getForObject(url, OpenMeteResponse.class))
                .orElseThrow(() -> new RuntimeException("Neuspjelo dohvaćanje podataka o satnoj prognozi"));
        log.info("Primljen OpenMeteo odgovor za 7 dana: {}", response);
        return response;
    }

    private OpenMeteResponse fetchOpenMeteoHourlyData(float lat, float lon) {
        String openMeteoUrl = "https://api.open-meteo.com/v1/forecast?";
        String url = UriComponentsBuilder.fromHttpUrl(openMeteoUrl)
                .queryParam("latitude", lat)
                .queryParam("longitude", lon)
                .queryParam("hourly", "temperature_2m,weather_code,uv_index,relativehumidity_2m,apparent_temperature,windspeed_10m,pressure_msl,visibility")
                .queryParam("forecast_days", 1)
                .toUriString();
        log.info("Zahtjev OpenMeteo za satne podatke s URL-om: {}", url);
        OpenMeteResponse response = Optional.ofNullable(restTemplate.getForObject(url, OpenMeteResponse.class))
                .orElseThrow(() -> new RuntimeException("Neuspjelo dohvaćanje podataka o satnoj prognozi"));
        log.info("Primljen OpenMeteo odgovor: {}", response);
        return response;
    }

    public WeatherResponse fetchWeatherData(CityEntity city) {
        String weatherBaseUrl = "https://api.openweathermap.org/data/2.5/weather?";
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(weatherBaseUrl)
                .queryParam("q", city.getName())
                .queryParam("units", "metric")
                .queryParam("appid", apiKey);
        return restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.GET,
                null,
                WeatherResponse.class
        ).getBody();
    }


    public float fetchUVData(float lat, float lng) {
        String openUvIndex = "https://api.openuv.io/api/v1/uv";
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(openUvIndex)
                .queryParam("lat", lat)
                .queryParam("lng", lng);

        log.info("UV Index URL: {}", uriBuilder.toUriString());
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-access-token", openUvKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        UVResponseDTO uvResponse = restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.GET,
                entity,
                UVResponseDTO.class
        ).getBody();
        assert uvResponse != null;
        log.info("UV Index odgovor: {}", uvResponse.result().uv());
        return uvResponse.result().uv();
    }


    public Coordinates getCordinates(CityEntity city) {
        String geocodingUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" + city.getName() + "&count=1";
        log.info("Geocoding URL: {}", geocodingUrl);
        ResponseEntity<OpenMeteoGoeCodingRes> restResponse = restTemplate.getForEntity(geocodingUrl, OpenMeteoGoeCodingRes.class);
        OpenMeteoGoeCodingRes geocodingResponse = restResponse.getBody();
        log.info("Odgovor tijela: {}", geocodingResponse.resultL().getFirst().name());
        if (geocodingResponse.resultL().isEmpty() || !Objects.equals(geocodingResponse.resultL().getFirst().name(), city.getName())) {
            throw new RuntimeException("Grad nije pronađen: " + city);
        }

        GeosResult result = geocodingResponse.resultL().getFirst();
        return new Coordinates(result.latitude(), result.longitude());
    }


    public List<WeatherForecastEntity> fetchAndSaveDailyForecast(CityEntity city) {
        return transactionTemplate.execute(status -> {
            try {
                WeatherResponse weatherResponse = fetchWeatherData(city);
                log.info("Odgovor o dnevnom vremenu za {}: {}", city, weatherResponse);
                return fetchAndSaveDailyForecastWithCoordinates(city, weatherResponse.cords().lat(), weatherResponse.cords().lng());
            } catch (Exception e) {
                log.warn("Neuspjelo dohvaćanje podataka o dnevnom vremenu za {} iz OpenWeatherMap, pokušavam geolokaciju", city);
                try {
                    Coordinates coords = getCordinates(city);
                    return fetchAndSaveDailyForecastWithCoordinates(city, coords.latitude(), coords.longitude());
                } catch (Exception geoException) {
                    log.error("Neuspjelo dohvaćanje i spremanje dnevne prognoze za {} korištenjem geolokacije", city, geoException);
                    throw new RuntimeException("Neuspjelo dohvaćanje i spremanje dnevne prognoze", geoException);
                }
            }
        });
    }


    private List<WeatherForecastEntity> fetchAndSaveDailyForecastWithCoordinates(CityEntity city, float lat, float lon) {
        OpenMeteResponse dailyResponse = fetchOpenMeteoDailyData(lat, lon);
        log.info("Dnevni odgovor: {}", dailyResponse);
        List<WeatherForecastEntity> forecasts = ProviderMapper.toDailyWeatherForecasts(city, dailyResponse);
        return forecastRepository.saveAll(forecasts);
    }

    public List<WeatherForecastEntity> fetchAndSaveHourlyForecast(CityEntity city) {
        try {
            WeatherResponse weatherResponse = fetchWeatherData(city);
            log.info("Odgovor o vremenu za {}: {}", city, weatherResponse);
            return fetchAndSaveHourlyForecastWithCoordinates(city, weatherResponse.cords().lat(), weatherResponse.cords().lng());
        } catch (Exception e) {
            log.warn("Neuspjelo dohvaćanje podataka o vremenu za {} iz OpenWeatherMap, pokušavam geolokaciju", city);
            Coordinates coords = getCordinates(city);
            return fetchAndSaveHourlyForecastWithCoordinates(city, coords.latitude(), coords.longitude());
        }
    }



    private List<WeatherForecastEntity> fetchAndSaveHourlyForecastWithCoordinates(CityEntity city, float lat, float lon) {
        OpenMeteResponse hourlyResponse = fetchOpenMeteoHourlyData(lat, lon);
        log.info("Satni odgovor: {}", hourlyResponse);
        List<WeatherForecastEntity> forecasts = ProviderMapper.toHourlyWeatherForecasts(city, hourlyResponse);
        return forecastRepository.saveAll(forecasts);
    }

    public WeatherForecastEntity fetchOpenMeteoWeather(CityEntity city, Coordinates coords) {
        OpenMeteResponse hourlyResponse = fetchOpenMeteoHourlyData(coords.latitude(), coords.longitude());
        int currentHourIndex = LocalDateTime.now().getHour();
        return ProviderMapper.toWeatherForecastFromOpenMeteo(city, hourlyResponse, currentHourIndex);
    }

/*
    @Transactional(readOnly = true)
    public WeatherForecastResponseDTO getWeatherFromOpenMeteo(String city) {
        CityEntity cityEntity = cityService.getOrCreateCity(city);
        Coordinates coords = getCordinates(cityEntity);
        OpenMeteResponse hourlyResponse = fetchOpenMeteoHourlyData(coords.latitude(), coords.longitude());
        int currentHourIndex = LocalDateTime.now().getHour();
        WeatherForecastEntity forecast = ProviderMapper.toWeatherForecastFromOpenMeteo(cityEntity, hourlyResponse, currentHourIndex);
        forecast.setForecastType(ForecastType.CURRENT);
        return mapper.convertToDTO(forecastRepository.save(forecast));
    }

 */

}
