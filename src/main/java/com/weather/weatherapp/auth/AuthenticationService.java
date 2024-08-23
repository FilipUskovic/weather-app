package com.weather.weatherapp.auth;

import com.weather.weatherapp.auth.dto.AuthenticationRequest;
import com.weather.weatherapp.auth.dto.AuthenticationResponse;
import com.weather.weatherapp.auth.dto.RegisterRequest;
import com.weather.weatherapp.auth.jtw.JwtService;
import com.weather.weatherapp.user.Role;
import com.weather.weatherapp.user.UserEntity;
import com.weather.weatherapp.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }


    public AuthenticationResponse registerAdmin(RegisterRequest request) {
        return register(request, Role.ADMIN);
    }

    public AuthenticationResponse registerUser(RegisterRequest request) {
        return register(request, Role.USER);
    }

    private AuthenticationResponse register(RegisterRequest request, Role role) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        var user = new UserEntity();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(role);
        userRepository.save(user);

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());

        String jwtToken = jwtService.generateToken(extraClaims, user);
        return new AuthenticationResponse(jwtToken);
    }


    public AuthenticationResponse authenticateEmailAndPassword(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(jwtToken);
    }


    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("Pokušaj autentikacije za korisnika: {}", request.email());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException e) {
            log.warn("Neuspješna autentikacija za korisnika: {}", request.email());
            throw new RuntimeException("Invalid username or password");
        }

        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.error("Korisnik nije pronađen u bazi: {}", request.email());
                    return new UsernameNotFoundException("User not found");
                });
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        log.info("claimssss {}", user.getRole().name());

        String jwtToken = jwtService.generateToken(extraClaims, user);
        log.info("Uspješna autentikacija za korisnika: {}", user.getEmail());
        return new AuthenticationResponse(jwtToken);
    }

}
