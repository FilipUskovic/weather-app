package com.weather.weatherapp.city;

import com.weather.weatherapp.city.dto.CityDTO;
import com.weather.weatherapp.city.dto.CityRequest;
import com.weather.weatherapp.exception.CityAlreadyExistsException;
import com.weather.weatherapp.exception.CityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CityDTO> createCity(@RequestBody CityRequest request) {
        try {
            CityDTO cityResponse = cityService.createCity(request);
            return new ResponseEntity<>(cityResponse, HttpStatus.CREATED);
        } catch (CityAlreadyExistsException ex) {
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<CityDTO>> getAllCities() {
        List<CityDTO> cities = cityService.getAllCities();
        if (cities.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(cities, HttpStatus.OK);
    }

    @GetMapping("/{name}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CityDTO> getCityByName(@PathVariable String name) {
        return cityService.getCityByName(name)
                .map(cityResponse -> new ResponseEntity<>(cityResponse, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<CityDTO> updateCity(@PathVariable Long id, @RequestBody CityRequest request) {
        try {
            CityDTO updatedCity = cityService.updateCity(id, request);
            return new ResponseEntity<>(updatedCity, HttpStatus.OK);
        } catch (CityNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> deleteCity(@PathVariable Long id) {
        try {
            cityService.deleteCity(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (CityNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
