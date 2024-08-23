package com.weather.weatherapp.weatherForecast;

import com.weather.weatherapp.city.CityEntity;
import com.weather.weatherapp.city.CityRepository;
import com.weather.weatherapp.city.CityService;
import com.weather.weatherapp.exception.CityNotFavoriteException;
import com.weather.weatherapp.exception.CityNotFoundException;
import com.weather.weatherapp.exception.UserNotFoundException;
import com.weather.weatherapp.exception.WeatherServiceException;
import com.weather.weatherapp.user.UserEntity;
import com.weather.weatherapp.user.UserRepository;
import com.weather.weatherapp.user.UserService;
import com.weather.weatherapp.weatherForecast.dto.*;
import com.weather.weatherapp.weatherForecast.dto.response.WeatherCastResponeDaily;
import com.weather.weatherapp.weatherForecast.dto.response.WeatherForecastResponseDTO;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;



@Service
public class WeatherForecastService {
    private static final Logger log = LoggerFactory.getLogger(WeatherForecastService.class);

    private final WeatherDataService weatherDataService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final MeterRegistry meterRegistry;
    private final CityService cityService;
    private final TransactionTemplate transactionTemplate;

    public WeatherForecastService(WeatherDataService weatherDataService,
                                  UserService userService,
                                  UserRepository userRepository,
                                  CityRepository cityRepository,
                                  MeterRegistry meterRegistry, CityService cityService, TransactionTemplate transactionTemplate){
        this.weatherDataService = weatherDataService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.cityRepository = cityRepository;
        this.meterRegistry = meterRegistry;
        this.cityService = cityService;
        this.transactionTemplate = transactionTemplate;
    }

    @Timed(value = "weather.forecast.fetch", description = "Vrijeme potrebno za dohvaćanje current vremenske prognoze")
    @Transactional
    public WeatherForecastResponseDTO getWeather(String city) {
        return weatherDataService.getWeather(city);
    }

    @Timed(value = "weather.forecast.fetch", description = "Vrijeme potrebno za dohvaćanje hourly vremenske prognoze")
    @Transactional
    public List<WeatherForecastResponseDTO> getHourly(String city) {
        meterRegistry.counter("weather.forecast.requests", "city", city).increment();
        return weatherDataService.getHourly(city);
    }

    @Timed(value = "weather.forecast.fetch", description = "Vrijeme potrebno za dohvaćanje daily vremenske prognoze")
    @Transactional(readOnly = true)
    public List<WeatherCastResponeDaily> getDaily(String city) {
        meterRegistry.counter("weather.forecast.requests", "city", city).increment();
        return weatherDataService.getDaily(city);
    }

    @Transactional(readOnly = true)
    public List<WeatherForecastResponseDTO> getFavoritesCitiesWeather(String username) {
        return transactionTemplate.execute(status -> userService.getFavoriteCities(username).stream()
                .map(this::getWeatherSafely)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()));

    }

    @Transactional
    public List<String> addFavoriteCity(String username, String city, String email) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Korisnik nije pronađen: " + username));
        CityEntity cityEntity = cityService.getOrCreateCity(city);
        if (!user.getFavoriteCities().contains(cityEntity)) {
            user.getFavoriteCities().add(cityEntity);
            user = userRepository.save(user);
        }
        user = userRepository.findById(user.getId()).orElseThrow(() -> new UserNotFoundException("Korisnik nije pronađen: " + username));


        return user.getFavoriteCities().stream()
                .map(CityEntity::getName)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<String> addFavoriteCityWithoutUsername(String email, String city) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Korisnik nije pronađen: " + email));

        CityEntity cityEntity = cityService.getOrCreateCity(city);

        if (!user.getFavoriteCities().contains(cityEntity)) {
            user.getFavoriteCities().add(cityEntity);
            userRepository.save(user);
        }

        return user.getFavoriteCities().stream()
                .map(CityEntity::getName)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    protected Optional<WeatherForecastResponseDTO> getWeatherSafely(String city) {
        try {
            return Optional.of(getWeather(city));
        } catch (WeatherServiceException e) {
            log.warn("Neuspjelo dohvaćanje vremena za grad {} putem OpenWeatherMap. Pokušavam s OpenMeteo.", city);
            try {
                return Optional.of(weatherDataService.getWeather(city));
            } catch (Exception e2) {
                log.warn("Neuspjelo dohvaćanje vremena za grad {} i putem OpenMeteo. Preskačem ovaj grad.", city);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.warn("Neuspjelo dohvaćanje vremena za grad: {}. Razlog: {}", city, e.getMessage());
            return Optional.empty();
        }
    }

    @Transactional
    public List<String> removeFavoriteCityWithoutWeather(String email, String cityName) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        CityEntity cityToRemove = user.getFavoriteCities().stream()
                .filter(city -> city.getName().equals(cityName))
                .findFirst()
                .orElseThrow(() -> new CityNotFoundException("City not found in favorites: " + cityName));

        user.getFavoriteCities().remove(cityToRemove);
        userRepository.save(user);

        return user.getFavoriteCities().stream()
                .map(CityEntity::getName)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeFavoriteCity(String username, String city) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Korisnik nije pronađen: " + username));

        CityEntity cityEntity = cityRepository.findByName(city)
                .orElseThrow(() -> new CityNotFoundException("Grad nije pronađen: " + city));

        if (!user.getFavoriteCities().contains(cityEntity)) {
            throw new CityNotFavoriteException("Grad " + city + " nije među omiljenim gradovima korisnika " + username);
        }

        user.getFavoriteCities().remove(cityEntity);
        userRepository.save(user);
        log.info("Uklonjen omiljeni grad {} za korisnika {}", city, username);
       // cacheService.deleteCachedData(CACHE_KEY_PREFIX + CURRENT_CACHE_PREFIX + city);
     //  cacheService.deleteCachedData(CACHE_KEY_PREFIX + HOURLY_CACHE_PREFIX + city);
      //  cacheService.deleteCachedData(CACHE_KEY_PREFIX + DAILY_CACHE_PREFIX + city);
    }


    public HistoricalDataEntry getHistoricalData(String city, LocalDate startDate, LocalDate endDate) {
        return weatherDataService.getHistoricalData(city, startDate, endDate);

    }

    @Transactional
    public List<String> getFavoriteCitiesByEmail(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Korisnik nije pronađen: " + email));

        return user.getFavoriteCities().stream()
                .map(CityEntity::getName)
                .collect(Collectors.toList());
    }
}