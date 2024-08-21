package com.weather.weatherapp.city;

import com.weather.weatherapp.city.dto.CityDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    // novo
    @GetMapping
    public ResponseEntity<List<CityDTO>> getAllCitiess() {
        return ResponseEntity.ok(cityService.getAllCitiess());
    }

    /* radi
    @GetMapping
    public ResponseEntity<List<CityEntity>> getAllCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }

     */
    //radi
    @GetMapping("/{name}")
    public ResponseEntity<CityEntity> getCityByName(@PathVariable String name) {
        return cityService.getCityByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
   // radi
    @PostMapping
    public ResponseEntity<CityEntity> createCity(@RequestParam String name) {
        return ResponseEntity.ok(cityService.createCity(name));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCity(@PathVariable Long id) {
        cityService.deleteCity(id);
        return ResponseEntity.ok().build();
    }
}
