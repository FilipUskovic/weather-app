package com.weather.weatherapp.auth;

import com.weather.weatherapp.auth.dto.AuthenticationRequest;
import com.weather.weatherapp.auth.dto.AuthenticationResponse;
import com.weather.weatherapp.auth.dto.LoginRequest;
import com.weather.weatherapp.auth.dto.RegisterRequest;
import com.weather.weatherapp.auth.jtw.JwtService;
import com.weather.weatherapp.city.CityEntity;
import com.weather.weatherapp.user.UserEntity;
import com.weather.weatherapp.user.UserRepository;
import com.weather.weatherapp.user.dto.response.UserResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);
    private final AuthenticationService authService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthenticationController(AuthenticationService authService, UserRepository userRepository, JwtService service) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.jwtService = service;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";  // Ovo će vratiti login.html template
    }

    @PostMapping("/admin")
    public ResponseEntity<AuthenticationResponse> loginAdmin(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registerAdmin(request));
    }


    @PreAuthorize("permitAll()")
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registerUser(request));
    }

    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            AuthenticationResponse response = authService.authenticateEmailAndPassword
                    (loginRequest.email(), loginRequest.password());
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Neispravni podaci za prijavu");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7).trim();
            try {
                jwtService.invalidateToken(jwt);
                log.info("Token uspješno poništen");
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                log.error("Greška pri poništavanju tokena", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            log.warn("Pokušaj odjave bez valjanog tokena");
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest request) {
        log.info("Authentication request received for user: {}", request.email());
        try {
            AuthenticationResponse response = authService.authenticate(request);
            log.info("Authentication successful for user: {}", request.email());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", request.email(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @GetMapping("/current-user")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserResponseDTO userDTO = new UserResponseDTO(
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getEmail(),
                userEntity.getFavoriteCities().stream().map(CityEntity::getName).collect(Collectors.toList())
        );
        return ResponseEntity.ok(userDTO);
    }
}
