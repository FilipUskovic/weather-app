package com.weather.weatherapp.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void addFavoriteCity(String username, String city) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.getFavoriteCities().add(city);
        userRepository.save(user);
    }


    @Transactional(readOnly = true)
    public List<String> getFavoriteCities(String username) {
        return userRepository.findByUsername(username)
                .map(user -> user.getFavoriteCities().stream().collect(Collectors.toList()))
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    @Transactional
    public void removeFavoriteCity(String username, String city) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
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
