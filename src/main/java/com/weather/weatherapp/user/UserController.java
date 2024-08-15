package com.weather.weatherapp.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/favorite")
    public ResponseEntity<Void> addFavoriteCity(@RequestParam String city, @RequestParam String username) {
        userService.addFavoriteCity(city,username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/add-user")
    public UserEntity addUser(@RequestParam String username){
       return userService.createUser(username);
    }
}
