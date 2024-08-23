package com.weather.weatherapp.user;

import com.weather.weatherapp.user.dto.UserInfo;
import com.weather.weatherapp.user.dto.request.AddFavoriteCityRequestDTO;
import com.weather.weatherapp.user.dto.request.CreateUserRequestDTO;
import com.weather.weatherapp.user.dto.response.UserResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{username}/favorites")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getFavoriteCities(@PathVariable String username) {
        return ResponseEntity.ok(userService.getFavoriteCities(username));
    }

    @PostMapping("/create-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody CreateUserRequestDTO requestDTO) {
        return ResponseEntity.ok(userService.createUser(requestDTO));
    }


   @PostMapping("/add-favorite-city")
   @PreAuthorize("isAuthenticated()")
   public ResponseEntity<UserResponseDTO> addFavoriteCity(@RequestBody AddFavoriteCityRequestDTO requestDTO) {
        log.info("add-favorite-city request: {}", requestDTO);
        return ResponseEntity.ok(userService.addFavoriteCity(requestDTO));
   }

    @DeleteMapping("/remove-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> removeUser(@RequestBody AddFavoriteCityRequestDTO requestDTO) {
        log.info("remove-user request: {}", requestDTO);
        return ResponseEntity.ok(userService.addFavoriteCity(requestDTO));
    }


    @GetMapping("/me2")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserInfo> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserInfo userInfo = new UserInfo(
                userDetails.getUsername(),
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
        );
        return ResponseEntity.ok(userInfo);
    }
}


