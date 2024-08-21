package com.weather.weatherapp.city;

import com.weather.weatherapp.city.dto.CityDTO;
import com.weather.weatherapp.city.dto.WeatherDTO;
import com.weather.weatherapp.user.UserEntity;
import com.weather.weatherapp.weatherForecast.WeatherForecastEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CityService {
    private static final Logger log = LoggerFactory.getLogger(CityService.class);
    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @Transactional(readOnly = true)
    public List<CityEntity> getAllCities() {
        log.info("getAllCities {}", cityRepository.findAll());
        return cityRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<CityEntity> getCityByName(String name) {
        return cityRepository.findByName(name);
    }

    @Transactional
    public CityEntity createCity(String name) {
        return cityRepository.save(new CityEntity(name));
    }

    @Transactional
    public CityEntity getOrCreateCity(String name) {
        return cityRepository.findByName(name)
                .orElseGet(() -> createCity(name));
    }

    @Transactional
    public void deleteCity(Long id) {
        cityRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean cityExists(String name) {
        return cityRepository.existsByName(name);
    }

    @Transactional
    public CityEntity updateCity(Long id, String newName) {
        CityEntity city = cityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("City not found: " + id));
        city.setName(newName);
        return cityRepository.save(city);
    }

    // novo

    public List<CityDTO> getAllCitiess() {
        List<CityEntity> cities = cityRepository.findAll();
        return cities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private CityDTO convertToDTO(CityEntity city) {
        // Konvertujte WeatherForecastEntity u WeatherForecastDTO
        List<WeatherDTO> forecasts = city.getWeatherForecasts().stream()
                .map(forecast -> new WeatherDTO(forecast.getId(), forecast.getDescription()))
                .collect(Collectors.toList());

        // Konvertujte UserEntity u Set<Long> ID-eve
        Set<Long> userIds = city.getUsers().stream()
                .map(UserEntity::getId)
                .collect(Collectors.toSet());

        // Kreirajte CityDTO
        return new CityDTO(
                        city.getId(),
                        city.getName(),
                forecasts,
                userIds
                );
    }

    private WeatherDTO convertToWeatherForecastDTO(WeatherForecastEntity forecast) {
       return new WeatherDTO(
               forecast.getId(),
               forecast.getDescription()
       );
    }
}
