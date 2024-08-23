package com.weather.weatherapp.weatherForecast;

import com.weather.weatherapp.auth.jtw.JwtService;
import com.weather.weatherapp.config.caching.WeatherCacheService;
import com.weather.weatherapp.exception.CityNotFavoriteException;
import com.weather.weatherapp.exception.CityNotFoundException;
import com.weather.weatherapp.exception.UserNotFoundException;
import com.weather.weatherapp.exception.WeatherServiceException;
import com.weather.weatherapp.user.dto.request.AddFavoriteCityRequestDTO;
import com.weather.weatherapp.weatherForecast.dto.HistoricalDataEntry;
import com.weather.weatherapp.weatherForecast.dto.response.WeatherCastResponeDaily;
import com.weather.weatherapp.weatherForecast.dto.response.WeatherForecastResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/weather")
public class WeatherForecastController {
    private static final Logger log = LoggerFactory.getLogger(WeatherForecastController.class);

    public final WeatherForecastService weatherForecastService;
    private final JwtService jwtService;
    private final WeatherAlertService weatherAlertService;
    private final WeatherCacheService weatherCacheService;

    public WeatherForecastController(WeatherForecastService weatherForecastService, JwtService jwtService, WeatherAlertService weatherAlertService, WeatherCacheService weatherCacheService) {
        this.weatherForecastService = weatherForecastService;
        this.jwtService = jwtService;
        this.weatherAlertService = weatherAlertService;
        this.weatherCacheService = weatherCacheService;
    }

    @GetMapping("/current/{city}")
    public ResponseEntity<WeatherForecastResponseDTO> getCurrentWeather(@PathVariable String city) {
        log.info("Zahtjev za trenutno vrijeme u gradu: {}", city);
        try {
            WeatherForecastResponseDTO forecast = weatherForecastService.getWeather(city);
            return ResponseEntity.ok(forecast);
        } catch (WeatherServiceException e) {
            log.error("Greška pri dohvaćanju trenutnog vremena za {}", city, e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            log.error("Neočekivana greška pri dohvaćanju trenutnog vremena za {}", city, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/hourly/{city}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<WeatherForecastResponseDTO>> getHourlyForecast(@PathVariable String city) {
        log.info("Zahtjev za satnu prognozu za grad: {}", city);
        try {
            List<WeatherForecastResponseDTO> forecast = weatherForecastService.getHourly(city);
            return forecast.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(forecast);
        } catch (Exception e) {
            log.error("Greška pri dohvaćanju satne prognoze za {}", city, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/daily/{city}")
    public ResponseEntity<List<WeatherCastResponeDaily>> getDailyForecast(@PathVariable String city) {
        log.info("Zahtjev za dnevnu prognozu za grad: {}", city);
        try {
            List<WeatherCastResponeDaily> forecast = weatherForecastService.getDaily(city);
            return ResponseEntity.ok(forecast);
        } catch (Exception e) {
            log.error("Greška pri dohvaćanju dnevne prognoze za {}", city, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/favorites")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getFavoriteCities(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7).trim();
            String email = jwtService.extractEmail(token);
            List<String> favorites = weatherForecastService.getFavoriteCitiesByEmail(email);
            return ResponseEntity.ok(favorites);
        } catch (UserNotFoundException e) {
            log.error("Korisnik nije pronađen pri dohvaćanju omiljenih gradova", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            log.error("Greška pri dohvaćanju omiljenih gradova", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }



    @GetMapping("/favorites-weather")
    public ResponseEntity<List<WeatherForecastResponseDTO>> getFavoritesCitiesWeather(@RequestParam String username) {
        log.info("Zahtjev za prognozu omiljenih gradova za korisnika: {}", username);
        try {
            List<WeatherForecastResponseDTO> forecasts = weatherForecastService.getFavoritesCitiesWeather(username);
            return ResponseEntity.ok(forecasts);
        } catch (UserNotFoundException e) {
            log.error("Korisnik nije pronađen: {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Greška pri dohvaćanju prognoza za omiljene gradove korisnika {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }




    @PostMapping("/favorites")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addFavoriteCity(@RequestBody AddFavoriteCityRequestDTO request,
                                             @RequestHeader("Authorization") String authHeader) {
        log.info("Primljen zahtjev za dodavanje omiljenog grada: {}", request);
        try {
            String token = authHeader.substring(7).trim();
            String email = jwtService.extractEmail(token);
            List<String> updatedFavorites = weatherForecastService.addFavoriteCityWithoutUsername(email, request.cityName());
            return ResponseEntity.ok(updatedFavorites);
        } catch (UserNotFoundException | CityNotFoundException e) {
            log.error("Greška pri dodavanju omiljenog grada", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Neočekivana greška pri dodavanju omiljenog grada", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Neuspjelo dodavanje omiljenog grada");
        }
    }



    @PostMapping("/favorites-weather")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addFavoriteCityWithWeather(@RequestBody AddFavoriteCityRequestDTO request,
                                             @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("Primljen zahtjev za dodavanje omiljenog grada: {}", request);
        try {
         //   String email = (authHeader != null) ? jwtService.extractEmail(authHeader) : "default@example.com";
            String email = jwtService.extractEmail(authHeader.substring(7));
            weatherForecastService.addFavoriteCity(request.username(), request.cityName(), email);
            return ResponseEntity.ok("Omiljeni grad uspješno dodan");
        } catch (UserNotFoundException | CityNotFoundException e) {
            log.error("Greška pri dodavanju omiljenog grada", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Neočekivana greška pri dodavanju omiljenog grada", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Neuspjelo dodavanje omiljenog grada");
        }
    }




    @DeleteMapping("/favorites-weather")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> removeFavoriteCityWeather(@RequestParam String username, @RequestParam String city) {
        log.info("Zahtjev za uklanjanje omiljenog grada {} za korisnika {}", city, username);
        try {
            weatherForecastService.removeFavoriteCity(username, city);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException | CityNotFoundException e) {
            log.error("Greška pri uklanjanju omiljenog grada", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (CityNotFavoriteException e) {
            log.error("Pokušaj uklanjanja grada koji nije omiljen", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Neočekivana greška pri uklanjanju omiljenog grada", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Neuspjelo uklanjanje omiljenog grada");
        }
    }

    @DeleteMapping("/favorites/{city}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> removeFavoriteCity(@PathVariable String city, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7).trim();
            String email = jwtService.extractEmail(token);
            List<String> updatedFavorites = weatherForecastService.removeFavoriteCityWithoutWeather(email, city);
            return ResponseEntity.ok(updatedFavorites);
        } catch (UserNotFoundException | CityNotFoundException e) {
            log.error("Error removing favorite city", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error removing favorite city", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to remove favorite city");
        }
    }

    @PostMapping("/send-email")
    public ResponseEntity<String> manuallyCheckAlerts() {
        log.info("Ručno pokrenuta provjera upozorenja");
        weatherAlertService.checkAndSendAlerts();
        return ResponseEntity.ok("Provjera upozorenja ručno pokrenuta");
    }

    @PostMapping("/refresh/{city}")
    public ResponseEntity<String> refreshWeatherData(@PathVariable String city) {
        log.info("Zahtjev za osvježavanje podataka za grad: {}", city);
        weatherCacheService.refreshHourlyForecast(city);
        weatherCacheService.refreshDailyForecast(city);
        return ResponseEntity.ok("Podaci osvježeni za grad: " + city);
    }

    @PostMapping("/clear-cache")
    public ResponseEntity<String> clearCache() {
        log.info("Zahtjev za čišćenje cache-a");
        weatherCacheService.clearAllCaches();
        return ResponseEntity.ok("Cache ociscen");

    }

    // ne pograni se u embed tablvicu historical_data
    @GetMapping("/historical/{city}")
    public ResponseEntity<HistoricalDataEntry> getHistoricalWeather(
            @PathVariable String city,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        HistoricalDataEntry data = weatherForecastService.getHistoricalData(city, startDate, endDate);
        return ResponseEntity.ok(data);
    }
}
