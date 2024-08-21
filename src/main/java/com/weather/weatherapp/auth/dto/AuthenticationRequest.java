package com.weather.weatherapp.auth.dto;

public record AuthenticationRequest(String username, String email, String password) {
}
