package com.weather.weatherapp.city;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CityService {
    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @Transactional(readOnly = true)
    public List<CityEntity> getAllCities() {
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
}
