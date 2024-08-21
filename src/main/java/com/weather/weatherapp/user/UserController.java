package com.weather.weatherapp.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // radi
    @PostMapping
    public ResponseEntity<UserEntity> createUser(@RequestParam String username) {
        return ResponseEntity.ok(userService.createUser(username));
    }
    // radio
    @PostMapping("favorites/{username}/")
    public ResponseEntity<Void> addFavoriteCity(@PathVariable String username, @RequestParam String city) {
        userService.addFavoriteCity(username, city);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{username}/remove-favorites")
    public ResponseEntity<Void> removeFavoriteCity(@PathVariable String username, @RequestParam String city) {
        userService.removeFavoriteCity(username, city);
        return ResponseEntity.ok().build();
    }
    //radi
    @GetMapping("/{username}/favorites")
    public ResponseEntity<List<String>> getFavoriteCities(@PathVariable String username) {
        return ResponseEntity.ok(userService.getFavoriteCities(username));
    }

}
