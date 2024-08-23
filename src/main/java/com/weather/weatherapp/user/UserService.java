package com.weather.weatherapp.user;

import com.weather.weatherapp.city.CityEntity;
import com.weather.weatherapp.city.CityRepository;
import com.weather.weatherapp.city.CityService;
import com.weather.weatherapp.exception.CityNotFoundException;
import com.weather.weatherapp.exception.DuplicateFavoriteCityException;
import com.weather.weatherapp.exception.UserAlreadyExistsException;
import com.weather.weatherapp.exception.UserNotFoundException;
import com.weather.weatherapp.user.dto.request.AddFavoriteCityRequestDTO;
import com.weather.weatherapp.user.dto.request.CreateUserRequestDTO;
import com.weather.weatherapp.user.dto.response.UserResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final CityService cityService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final CityRepository cityRepository;

    public UserService(UserRepository userRepository, CityService cityService, PasswordEncoder passwordEncoder, UserMapper userMapper, CityRepository cityRepository) {
        this.userRepository = userRepository;
        this.cityService = cityService;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.cityRepository = cityRepository;
    }

    @Transactional
    public UserResponseDTO createUser(CreateUserRequestDTO request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("Korisnik s korisničkim imenom "
                    + request.username() + " već postoji.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Korisnik s e-mail adresom " +
                    request.email() + " već postoji.");
    }

        UserEntity newUser = new UserEntity();
        newUser.setUsername(request.username());
        newUser.setEmail(request.email());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setRole(Role.USER);
        UserEntity savedUser = userRepository.save(newUser);
        log.info("Kreiran novi korisnik: {}", savedUser.getRealUsername());
        return userMapper.convertToDTO(savedUser);
    }


    @Transactional
    public UserResponseDTO addFavoriteCity(AddFavoriteCityRequestDTO request) {
        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UserNotFoundException("Korisnik nije pronađen: " + request.username()));
        CityEntity city = cityService.getOrCreateCity(request.cityName());
        if (user.getFavoriteCities().contains(city)) {
            throw new DuplicateFavoriteCityException("Grad " + request.cityName() + " je već omiljeni grad za korisnika " + request.username());
        }

        user.addFavoriteCity(city);
        UserEntity updatedUser = userRepository.save(user);
        log.info("Dodan omiljeni grad {} za korisnika {}", request.cityName(), request.username());
        return userMapper.convertToDTO(updatedUser);

    }

    @Transactional(readOnly = true)
    public List<String> getFavoriteCities(String username) {
        return userRepository.findByUsername(username)
                .map(user -> user.getFavoriteCities().stream()
                        .map(CityEntity::getName)
                        .collect(Collectors.toList()))
                .orElseThrow(() -> new UserNotFoundException("Korisnik nije pronađen: " + username));

    }

    @Transactional
    public void removeFavoriteCity(String username, String cityName) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Korisnik nije pronađen: " + username));
        CityEntity city = cityRepository.findByName(cityName)
                .orElseThrow(() -> new CityNotFoundException("Grad nije pronađen: " + cityName));
        user.removeFavoriteCity(city);
        userRepository.save(user);
        log.info("Uklonjen omiljeni grad {} za korisnika {}", cityName, username);
    }
}
