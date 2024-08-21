package com.weather.weatherapp.weatherForecast;

import com.weather.weatherapp.city.CityEntity;
import com.weather.weatherapp.city.CityRepository;
import com.weather.weatherapp.city.CityService;
import com.weather.weatherapp.config.caching.CachingRedisService;
import com.weather.weatherapp.user.Role;
import com.weather.weatherapp.user.UserEntity;
import com.weather.weatherapp.user.UserRepository;
import com.weather.weatherapp.user.UserService;
import com.weather.weatherapp.weatherForecast.dto.*;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class WeatherForecastService {
    private static final Logger log = LoggerFactory.getLogger(WeatherForecastService.class);

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
    private final TransactionTemplate transactionTemplate;
    private final UserService userService;
    private final CityService cityService;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final CachingRedisService cacheService;
    private static final String CACHE_KEY_PREFIX = "weather:";
    private static final long CACHE_TTL = 3600; // 1 sat u sekunda
    private final MeterRegistry meterRegistry;



    public WeatherForecastService(WeatherForecastRepository weatherForecastRepository,
                                  RestTemplate restTemplate,
                                  @Value("${weather_api_key}") String apiKey,
                                  @Value("${open_UV_index}") String openUvKey,
                                  TransactionTemplate transactionTemplate,
                                  UserService userService,
                                  CityService cityService,
                                  UserRepository userRepository,
                                  CityRepository cityRepository,
                                  CachingRedisService cacheService, MeterRegistry meterRegistry) {
        this.forecastRepository = weatherForecastRepository;
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.openUvKey = openUvKey;
        this.transactionTemplate = transactionTemplate;
        this.userService = userService;
        this.cityService = cityService;
        this.userRepository = userRepository;
        this.cityRepository = cityRepository;
        this.cacheService = cacheService;
        this.meterRegistry = meterRegistry;
    }


    // Dohvaca trenutno vrijeme koristi dva vanjsa api
    // TODO: Razmimisli o podjeli metode na getCurrect Weather iz base ako nije starije od 1h
    //TODO: Na taj nacin bi mogli optimizirati hibernate i ovo bi bilo Transational(readonly onda)
    // TODO: dodati koridante za opemMeteo ako ne radi kordiante opemWeeather tj ako se ne moze pronaci grad
    @Transactional
    public WeatherForecastEntity getWeather(String city) {
        String cacheKey = CACHE_KEY_PREFIX + city;
        WeatherForecastEntity cachedForecast = (WeatherForecastEntity) cacheService.getCachedData(cacheKey);
        if (cachedForecast != null) {
            log.info("Returning cached weather data for {}", city);
            return cachedForecast;
        }
        CityEntity cityEntity = cityService.getOrCreateCity(city);
        WeatherResponse responseWeather = fetchWeatherData(cityEntity);
        if (responseWeather == null) {
            throw new RuntimeException("Failed to fetch weather data for cityEntity: " + city);
        }
        System.out.println("weather response " + responseWeather);
        float uvIndex = fetchUVData(responseWeather.cords().lat(), responseWeather.cords().lng());
        WeatherForecastEntity forecast = WeatherMapper.toWeatherForecast(cityEntity, responseWeather, uvIndex);
        forecast.setForecastType(ForecastType.CURRENT);
        // chance
        cacheService.cacheData(cacheKey, forecast, CACHE_TTL);
        log.info("Cached new weather data for {}", city);
        return forecastRepository.save(forecast);
    }

    @Timed(value = "weather.forecast.fetch", description = "Time taken to fetch weather forecast")
    @Transactional
    public List<WeatherForecastEntity> getHourly(String city){
        CityEntity cityEntity = cityService.getOrCreateCity(city);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.plusHours(24);
        Optional<List<WeatherForecastEntity>> existingForeCastOpt =
                forecastRepository.findByCityAndForecastTypeAndDateTimeBetween(String.valueOf(cityEntity), ForecastType.HOURLY, now, end);
        meterRegistry.counter("weather.forecast.requests", "city", city).increment();
        if(existingForeCastOpt.isPresent() && !existingForeCastOpt.get().isEmpty()){
            log.info("Returning existing hourly forecasts for {}", city);
            return existingForeCastOpt.get();
        }else {
            log.info("Fetching new hourly forecasts for {}", city);
            return fetchAndSaveHourlyForecast(cityEntity);
        }
    }



    @Transactional(readOnly = true)
    public List<WeatherForecastEntity> getDaily(String city) {
        return transactionTemplate.execute(status -> {
        CityEntity cityEntity = cityService.getOrCreateCity(city);
        LocalDate now = LocalDate.now();
        LocalDate end = now.plusDays(7);

        LocalDateTime startDateTime = now.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59);


        Optional<List<WeatherForecastEntity>> existingForecastOpt =
                forecastRepository.findByCityAndForecastTypeAndDateTimeBetween(String.valueOf(cityEntity), ForecastType.DAILY, startDateTime, endDateTime);
        if (existingForecastOpt.isPresent() && !existingForecastOpt.get().isEmpty()) {
            log.info("Returning existing daily forecasts for {}", city);
            return existingForecastOpt.get();
        } else {
            log.info("Fetching new daily forecasts for {}", city);
            return fetchAndSaveDailyForecast(cityEntity);
        }
        });
    }


    @Transactional(readOnly = true)
    public List<WeatherForecastEntity> getFavoritesCitiesWeather(String username) {
        return transactionTemplate.execute(status -> {
            try {
                List<String> favoriteCities = userService.getFavoriteCities(username);
                return favoriteCities.stream()
                        .map(this::getWeather)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                log.warn("failed to get faforits cities data za username-a{}", username);
            }
            return List.of();
        });
    }

    @Transactional
    public void addFavoriteCity(String username, String city, String email) {
            log.info("Adding favorite cityEntity {} for user {}", city, username);

            UserEntity user = userRepository.findByUsername(username)
                    .orElseGet(() -> {
                        log.info("User {} not found. Creating new user.", username);
                        UserEntity newUser = new UserEntity();
                        newUser.setUsername(username);
                        newUser.setEmail(email);
                        newUser.setRole(Role.USER);
                        newUser.setPassword("defaultPassword");
                        return userRepository.save(newUser);
                    });

            CityEntity cityEntity = cityRepository.findByName(city)
                    .orElseGet(() -> {
                        CityEntity newCity = new CityEntity();
                        newCity.setName(city);
                        return cityRepository.save(newCity);
                    });

        log.info("User favorite cities before adding: {}", user.getFavoriteCities());
        log.info("City to addd: {}", cityEntity);

        if (!user.getFavoriteCities().contains(cityEntity)) {
            user.getFavoriteCities().add(cityEntity);
            userRepository.save(user);
            log.info("Successfully added {} to favorites for user {}", city, username);
        } else {
            log.info("City {} is already a favorite for user {}", city, username);
        }
        log.info("User favorite cities after adding: {}", user.getFavoriteCities());

    }

    @Transactional
    public void removeFavoriteCity(String username, String city) {
        userService.removeFavoriteCity(username, city);
    }

    protected List<WeatherForecastEntity> fetchAndSaveDailyForecast(CityEntity city) {
        return transactionTemplate.execute(status -> {
            try {
                WeatherResponse weatherResponse = fetchWeatherData(city);
                log.info("Weather response daily for {}: {}", city, weatherResponse);
                return fetchAndSaveDailyForecastWithCoordinates(city, weatherResponse.cords().lat(), weatherResponse.cords().lng());
            } catch (Exception e) {
                log.warn("Failed to fetch weather data daily for {} from OpenWeatherMap, trying geolocation", city);
                try {
                    Coordinates coords = getCordinates(city);
                    return fetchAndSaveDailyForecastWithCoordinates(city, coords.latitude(), coords.longitude());
                } catch (Exception geoException) {
                    log.error("Failed to fetch and save daily forecast for {} using geolocation", city, geoException);
                    throw new RuntimeException("Failed to fetch and save daily forecast", geoException);
                }
            }
        });
    }

    private List<WeatherForecastEntity> fetchAndSaveDailyForecastWithCoordinates(CityEntity city, float lat, float lon) {
        OpenMeteResponse dailyResponse = fetchOpenMeteoDailyData(lat, lon);
        log.info("Daily response: {}", dailyResponse);
        List<WeatherForecastEntity> forecasts = WeatherMapper.toDailyWeatherForecasts(city, dailyResponse);
        return forecastRepository.saveAll(forecasts);
    }


    private OpenMeteResponse fetchOpenMeteoDailyData(float lat, float lon) {
        String openMeteoUrl = "https://api.open-meteo.com/v1/forecast?";
        String url = UriComponentsBuilder.fromHttpUrl(openMeteoUrl)
                .queryParam("latitude", lat)
                .queryParam("longitude", lon)
                .queryParam("daily", "temperature_2m_max,temperature_2m_min,weather_code,apparent_temperature_max,relative_humidity_2m_max,windspeed_10m_max,uv_index_max")
                // .queryParam("daily", "temperature_2m_max,weather_code,apparent_temperature_max,windspeed_10m_max,uv_index_max")
                .queryParam("forecast_days", 7)
                .toUriString();
        log.info("Requesting OpenMeteo for 7 day data with URL: {}", url);
        OpenMeteResponse response = Optional.ofNullable(restTemplate.getForObject(url, OpenMeteResponse.class))
                .orElseThrow(() -> new RuntimeException("Failed to fetch hourly forecast data"));
        log.info("Received OpenMeteo response for 7 days: {}", response);
        return response;
    }

    private List<WeatherForecastEntity> fetchAndSaveHourlyForecast(CityEntity city) {
        try {
            WeatherResponse weatherResponse = fetchWeatherData(city);
            log.info("Weather response for {}: {}", city, weatherResponse);
            return fetchAndSaveHourlyForecastWithCoordinates(city, weatherResponse.cords().lat(), weatherResponse.cords().lng());
        } catch (Exception e) {
            log.warn("Failed to fetch weather data for {} from OpenWeatherMap, trying geolocation", city);
            Coordinates coords = getCordinates(city);
            return fetchAndSaveHourlyForecastWithCoordinates(city, coords.latitude(), coords.longitude());
        }
    }


    private List<WeatherForecastEntity> fetchAndSaveHourlyForecastWithCoordinates(CityEntity city, float lat, float lon) {
        OpenMeteResponse hourlyResponse = fetchOpenMeteoHourlyData(lat, lon);
        log.info("Hourly response: {}", hourlyResponse);
        List<WeatherForecastEntity> forecasts = WeatherMapper.toHourlyWeatherForecasts(city, hourlyResponse);
        return forecastRepository.saveAll(forecasts);
    }

    private OpenMeteResponse fetchOpenMeteoHourlyData(float lat, float lon) {
        String openMeteoUrl = "https://api.open-meteo.com/v1/forecast?";
        String url = UriComponentsBuilder.fromHttpUrl(openMeteoUrl)
                .queryParam("latitude", lat)
                .queryParam("longitude", lon)
                .queryParam("hourly", "temperature_2m,weather_code,uv_index,relativehumidity_2m,apparent_temperature,windspeed_10m,pressure_msl,visibility")
                .queryParam("forecast_days", 1)
                .toUriString();
        log.info("Requesting OpenMeteo hourly data with URL: {}", url);
        OpenMeteResponse response = Optional.ofNullable(restTemplate.getForObject(url, OpenMeteResponse.class))
                .orElseThrow(() -> new RuntimeException("Failed to fetch hourly forecast data"));
        log.info("Received OpenMeteo response: {}", response);
        return  response;
    }


    WeatherResponse fetchWeatherData(CityEntity city) {
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

    float fetchUVData(float lat, float lng) {
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
        assert uvResponse != null;
        System.out.println("urrr " + uvResponse.result().uv());
        return uvResponse.result().uv();
    }

    Coordinates getCordinates(CityEntity city){
        String encodedCityName;
        encodedCityName = URLEncoder.encode(city.getName(), StandardCharsets.UTF_8);
        String geocodingUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" + city.getName() + "&count=1";
        log.info("Geocoding URL: {}", geocodingUrl);
        ResponseEntity<OpenMeteoGoeCodingRes> restResponse = restTemplate.getForEntity(geocodingUrl, OpenMeteoGoeCodingRes.class);
        OpenMeteoGoeCodingRes geocodingResponse = restResponse.getBody();
        log.info("Response body: {}", geocodingResponse.resultL().getFirst().name());
        if (geocodingResponse.resultL().isEmpty() || !Objects.equals(geocodingResponse.resultL().getFirst().name(), city.getName())) {
            throw new RuntimeException("City not found: " + city);
        }

        GeosResult result = geocodingResponse.resultL().getFirst();
        return new Coordinates(result.latitude(), result.longitude());
    }

}

