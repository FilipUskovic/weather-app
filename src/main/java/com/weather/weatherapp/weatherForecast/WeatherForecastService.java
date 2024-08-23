package com.weather.weatherapp.weatherForecast;

import com.weather.weatherapp.city.CityEntity;
import com.weather.weatherapp.city.CityRepository;
import com.weather.weatherapp.city.CityService;
import com.weather.weatherapp.config.caching.CachingRedisService;
import com.weather.weatherapp.exception.CityNotFavoriteException;
import com.weather.weatherapp.exception.CityNotFoundException;
import com.weather.weatherapp.exception.UserNotFoundException;
import com.weather.weatherapp.exception.WeatherServiceException;
import com.weather.weatherapp.providers.ProviderMapper;
import com.weather.weatherapp.providers.WeatherProviderServis;
import com.weather.weatherapp.providers.dto.Coordinates;
import com.weather.weatherapp.user.Role;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// import static com.weather.weatherapp.weatherForecast.WeatherCacheService.CACHE_KEY_PREFIX;


@Service
public class WeatherForecastService {
    private static final Logger log = LoggerFactory.getLogger(WeatherForecastService.class);

    private final WeatherDataService weatherDataService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final MeterRegistry meterRegistry;

    public WeatherForecastService(WeatherDataService weatherDataService,
                                  UserService userService,
                                  UserRepository userRepository,
                                  CityRepository cityRepository,
                                  MeterRegistry meterRegistry){
        this.weatherDataService = weatherDataService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.cityRepository = cityRepository;
        this.meterRegistry = meterRegistry;

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
        return userService.getFavoriteCities(username).stream()
                .map(this::getWeatherSafely)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addFavoriteCity(String username, String city, String email) {
        // Existing implementation...
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

    /*

    private final WeatherForecastRepository forecastRepository;
    private final TransactionTemplate transactionTemplate;
    private final UserService userService;
    private final CityService cityService;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final CachingRedisService cacheService;
    private static final String CACHE_KEY_PREFIX = "weather:";
    private static final String HOURLY_CACHE_PREFIX = "hourly:";
    private static final String DAILY_CACHE_PREFIX = "daily:";
    private static final String CURRENT_CACHE_PREFIX = "current:";
    private static final long CACHE_TTL = 3600; // 1 sat u sekunda
    private final MeterRegistry meterRegistry;
    private final WeatherMapper mapper;
    private final WeatherProviderServis weatherProvider;
    private final WeatherCacheService weatherCacheService;


    public WeatherForecastService(WeatherForecastRepository weatherForecastRepository,
                                  TransactionTemplate transactionTemplate,
                                  UserService userService,
                                  CityService cityService,
                                  UserRepository userRepository,
                                  CityRepository cityRepository,
                                  CachingRedisService cacheService,
                                  MeterRegistry meterRegistry,
                                  WeatherMapper mapper,
                                  WeatherProviderServis weatherProvider, WeatherCacheService weatherCacheService) {
        this.forecastRepository = weatherForecastRepository;
        this.transactionTemplate = transactionTemplate;
        this.userService = userService;
        this.cityService = cityService;
        this.userRepository = userRepository;
        this.cityRepository = cityRepository;
        this.cacheService = cacheService;
        this.meterRegistry = meterRegistry;
        this.mapper = mapper;
        this.weatherProvider = weatherProvider;
        this.weatherCacheService = weatherCacheService;
    }

    @Timed(value = "weather.forecast.fetch", description = "Vrijeme potrebno za dohvaćanje current vremenske prognoze")
    @Transactional
    public WeatherForecastResponseDTO getWeather(String city) {
        String cacheKey = CACHE_KEY_PREFIX + CURRENT_CACHE_PREFIX + city;
        Object cachedData = cacheService.getCachedData(cacheKey);
        if (cachedData instanceof WeatherForecastCacheDTO) {
            log.info("Vraćam podatke o vremenu iz cachea za {}", city);
            WeatherForecastEntity cachedForecast = ((WeatherForecastCacheDTO) cachedData).toEntity();
            return mapper.convertToDTO(cachedForecast);
        }
        CityEntity cityEntity = cityService.getOrCreateCity(city);
        WeatherForecastEntity forecast;
        try {
            WeatherResponse responseWeather = weatherProvider.fetchWeatherData(cityEntity);
            if (responseWeather == null) {
                throw new WeatherServiceException("Neuspjelo dohvaćanje podataka o vremenu za grad: " + city);
            }
            log.info("Odgovor o vremenu {}", responseWeather);
            float uvIndex = weatherProvider.fetchUVData(responseWeather.cords().lat(), responseWeather.cords().lng());
            forecast = ProviderMapper.toWeatherForecast(cityEntity, responseWeather, uvIndex);
        } catch (Exception e) {
            log.warn("Neuspjelo dohvaćanje podataka s OpenWeatherMap za {}. Pokušavam s geolokacijom.", city);
            try {
                Coordinates coords = weatherProvider.getCordinates(cityEntity);
                forecast = weatherProvider.fetchOpenMeteoWeather(cityEntity, coords);
            } catch (Exception geoException) {
                log.error("Neuspjelo dohvaćanje vremena za {} korištenjem geolokacije", city, geoException);
                throw new RuntimeException("Neuspjelo dohvaćanje vremenske prognoze", geoException);
            }
        }
        forecast.setForecastType(ForecastType.CURRENT);
        WeatherForecastCacheDTO cacheDTO = WeatherForecastCacheDTO.fromEntity(forecast);
        cacheService.cacheData(cacheKey, cacheDTO, CACHE_TTL);

        log.info("Spremljeni novi podaci o vremenu za getWeather {} u cache", city);
        return mapper.convertToDTO(forecastRepository.save(forecast));
    }


    @Timed(value = "weather.forecast.fetch", description = "Vrijeme potrebno za dohvaćanje hourly vremenske prognoze")
    @Transactional
    public List<WeatherForecastResponseDTO> getHourly(String city) {
        String cacheKey = CACHE_KEY_PREFIX + HOURLY_CACHE_PREFIX + city;
        Object cachedData = cacheService.getCachedData(cacheKey);
        if (cachedData instanceof List<?> cachedList) {
            if (!cachedList.isEmpty() && cachedList.getFirst() instanceof WeatherForecastResponseDTO) {
                log.info("Vraćam satnu prognozu iz cachea za {}", city);
                return (List<WeatherForecastResponseDTO>) cachedList;
            }
        }

        if (cachedData instanceof WeatherForecastResponseDTO) {
            log.info("Vraćam satnu prognozu iz cachea za {}", city);
            return (List<WeatherForecastResponseDTO>) cachedData;
        }

        CityEntity cityEntity = cityService.getOrCreateCity(city);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.plusHours(24);
        Optional<List<WeatherForecastEntity>> existingForeCastOpt =
                forecastRepository.findByCityAndForecastTypeAndDateTimeBetween(String.valueOf(cityEntity), ForecastType.HOURLY, now, end);
        meterRegistry.counter("weather.forecast.requests", "city", city).increment();
        List<WeatherForecastResponseDTO> result;
        if (existingForeCastOpt.isPresent() && !existingForeCastOpt.get().isEmpty()) {
            log.info("Vraćam postojeće satne prognoze za {}", city);
            result = existingForeCastOpt.get().stream().map(mapper::convertToDTO).collect(Collectors.toList());
        } else {
            log.info("Dohvaćam nove satne prognoze za {}", city);
            result = weatherProvider.fetchAndSaveHourlyForecast(cityEntity).stream().map(mapper::convertToDTO).collect(Collectors.toList());
        }

        cacheService.cacheData(cacheKey, result, CACHE_TTL);
        return result;
    }
    @Timed(value = "weather.forecast.fetch", description = "Vrijeme potrebno za dohvaćanje daily vremenske prognoze")
    @Transactional(readOnly = true)
    public List<WeatherCastResponeDaily> getDaily(String city) {
        String cacheKey = CACHE_KEY_PREFIX + DAILY_CACHE_PREFIX + city;
        Object cachedData = cacheService.getCachedData(cacheKey);
        if (cachedData instanceof List<?> cachedList) {
            if (!cachedList.isEmpty() && cachedList.getFirst() instanceof WeatherForecastResponseDTO) {
                log.info("Vraćam dnevnu prognozu iz cachea za {}", city);
                return (List<WeatherCastResponeDaily>) cachedList;
            }
        }
        return transactionTemplate.execute(status -> {
            CityEntity cityEntity = cityService.getOrCreateCity(city);
            LocalDate now = LocalDate.now();
            LocalDate end = now.plusDays(7);

            LocalDateTime startDateTime = now.atStartOfDay();
            LocalDateTime endDateTime = end.atTime(23, 59, 59);

            Optional<List<WeatherForecastEntity>> existingForecastOpt =
                    forecastRepository.findByCityAndForecastTypeAndDateTimeBetween(String.valueOf(cityEntity), ForecastType.DAILY, startDateTime, endDateTime);
            List<WeatherCastResponeDaily> result;
            meterRegistry.counter("weather.forecast.requests", "city", city).increment();
            if (existingForecastOpt.isPresent() && !existingForecastOpt.get().isEmpty()) {
                log.info("Vraćam postojeće dnevne prognoze za {}", city);
                result = existingForecastOpt.get().stream().map(mapper::convertToDTODaily).collect(Collectors.toList());
            } else {
                log.info("Dohvaćam nove dnevne prognoze za {}", city);
                result = weatherProvider.fetchAndSaveDailyForecast(cityEntity).stream().map(mapper::convertToDTODaily).collect(Collectors.toList());
            }

            cacheService.cacheData(cacheKey, result, CACHE_TTL);
            return result;
        });
    }

    @Transactional(readOnly = true)
    public List<WeatherForecastResponseDTO> getFavoritesCitiesWeather(String username) {
        return transactionTemplate.execute(status -> {
            List<String> favoriteCities = userService.getFavoriteCities(username);
            return favoriteCities.stream()
                    .map(this::getWeatherSafely)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        });
    }

    @Transactional
    public void addFavoriteCity(String username, String city, String email) {
        log.info("Dodavanje omiljenog grada {} za korisnika {}", city, username);

        UserEntity user = userRepository.findByUsername(username)
                .orElseGet(() -> {
                    log.info("Korisnik {} nije pronađen. Stvaram novog korisnika.", username);
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

        log.info("Omiljeni gradovi korisnika prije dodavanja: {}", user.getFavoriteCities());
        log.info("Grad za dodati: {}", cityEntity);

        if (!user.getFavoriteCities().contains(cityEntity)) {
            user.getFavoriteCities().add(cityEntity);
            userRepository.save(user);
            log.info("Uspješno dodan {} u omiljene za korisnika {}", city, username);
            cacheService.deleteCachedData(CACHE_KEY_PREFIX + CURRENT_CACHE_PREFIX + city);
            cacheService.deleteCachedData(CACHE_KEY_PREFIX + HOURLY_CACHE_PREFIX + city);
            cacheService.deleteCachedData(CACHE_KEY_PREFIX + DAILY_CACHE_PREFIX + city);

        } else {
            log.info("Grad {} je već omiljen za korisnika {}", city, username);
        }
        log.info("Omiljeni gradovi korisnika nakon dodavanja: {}", user.getFavoriteCities());
    }


    @Transactional(readOnly = true)
    protected Optional<WeatherForecastResponseDTO> getWeatherSafely(String city) {
        try {
            return Optional.of(getWeather(city));
        } catch (WeatherServiceException e) {
            log.warn("Neuspjelo dohvaćanje vremena za grad {} putem OpenWeatherMap. Pokušavam s OpenMeteo.", city);
            try {
                return Optional.of(weatherProvider.getWeatherFromOpenMeteo(city));
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
        cacheService.deleteCachedData(CACHE_KEY_PREFIX + CURRENT_CACHE_PREFIX + city);
        cacheService.deleteCachedData(CACHE_KEY_PREFIX + HOURLY_CACHE_PREFIX + city);
        cacheService.deleteCachedData(CACHE_KEY_PREFIX + DAILY_CACHE_PREFIX + city);
    }


    public HistoricalDataEntry getHistoricalData(String city, LocalDate startDate, LocalDate endDate) {
        String cacheKey = "historical:" + city + ":" + startDate + ":" + endDate;

        // Prvo pokušaj dohvatiti podatke iz cachea
        HistoricalDataEntry cachedData = weatherCacheService.getCachedHistoricalData(cacheKey);
        if (cachedData != null) {
            log.info("Vraćam povijesne podatke iz cachea za grad: {} od {} do {}", city, startDate, endDate);
            return cachedData;
        }
        // Ako nema u cacheu, dohvati nove podatke
        Coordinates coords = weatherProvider.getCordinates(new CityEntity(city));
        HistoricalDataEntry historicalData = weatherProvider.fetchOpenMeteoHistoricalData(
                coords.latitude(), coords.longitude(), startDate, endDate);

        // Spremi nove podatke u cache
        weatherCacheService.cacheHistoricalData(cacheKey, historicalData);

        return historicalData;
    }
    */

}