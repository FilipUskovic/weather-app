package com.weather.weatherapp.city;

import com.weather.weatherapp.city.dto.CityDTO;
import com.weather.weatherapp.city.dto.CityRequest;
import com.weather.weatherapp.exception.CityAlreadyExistsException;
import com.weather.weatherapp.exception.CityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CityService {
    private static final Logger log = LoggerFactory.getLogger(CityService.class);
    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

    public CityService(CityRepository cityRepository, CityMapper cityMapper) {
        this.cityRepository = cityRepository;
        this.cityMapper = cityMapper;
    }

    public CityEntity getOrCreateCity(String name) {
        return cityRepository.findByName(name)
                .orElseGet(() -> {
                    CityEntity newCity = new CityEntity(name);
                    log.info("Kreiran novi grad: {}", name);
                    return cityRepository.save(newCity);
                });
    }

    @Transactional(readOnly = true)
    public List<CityDTO> getAllCities() {
        log.info("Dohvaćanje svih gradova");
        return cityRepository.findAll().stream()
                .map(cityMapper::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<CityDTO> getCityByName(String name) {
        log.info("Traženje grada po imenu: {}", name);
        return cityRepository.findByName(name)
                .map(cityMapper::convertToDTO);
    }

    @Transactional
    public CityDTO createCity(CityRequest request) {
        String name = request.city();
        if (cityRepository.existsByName(name)) {
            throw new CityAlreadyExistsException("Grad s imenom " + name + " već postoji.");
        }
        CityEntity cityEntity = new CityEntity(name);
        log.info("Kreiranje novog grada: {}", name);
        return cityMapper.convertToDTO(cityRepository.save(cityEntity));
    }


    @Transactional
    public void deleteCity(Long id) {
        if (!cityRepository.existsById(id)) {
            throw new CityNotFoundException("Grad s ID " + id + " nije pronađen.");
        }
        log.info("Brisanje grada s ID: {}", id);
        cityRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean cityExists(String name) {
        boolean exists = cityRepository.existsByName(name);
        log.info("Provjera postoji li grad {}: {}", name, exists);
        return exists;
    }

    @Transactional
    public CityDTO updateCity(Long id, CityRequest request) {
        CityEntity city = cityRepository.findById(id)
                .orElseThrow(() -> new CityNotFoundException("Grad nije pronađen: " + id));
        city.setName(request.city());
        CityEntity updatedCity = cityRepository.save(city);
        log.info("Ažuriran grad s ID {}: novo ime je {}", id, request.city());
        return cityMapper.convertToDTO(updatedCity);
    }

}
