package com.weather.weatherapp.user;

import com.weather.weatherapp.city.CityEntity;
import com.weather.weatherapp.city.CityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CityService cityService;

    public UserService(UserRepository userRepository, CityService cityService) {
        this.userRepository = userRepository;
        this.cityService = cityService;
    }

    @Transactional
    public void addFavoriteCity(String username, String cityName) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        CityEntity city = cityService.getOrCreateCity(cityName);
        user.addFavoriteCity(city);
        userRepository.save(user);
    }


    @Transactional(readOnly = true)
    public List<String> getFavoriteCities(String username) {
        return userRepository.findByUsername(username)
                .map(user -> user.getFavoriteCities().stream()
                        .map(CityEntity::getName)
                        .collect(Collectors.toList()))
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    @Transactional
    public void removeFavoriteCity(String username, String cityName) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        CityEntity city = cityService.getCityByName(cityName)
                .orElseThrow(() -> new RuntimeException("City not found: " + cityName));
        user.removeFavoriteCity(city);
        userRepository.save(user);
    }

    @Transactional
    public UserEntity createUser(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists: " + username);
        }
        UserEntity newUser = new UserEntity(username);
        return userRepository.save(newUser);
    }
}
