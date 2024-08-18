package com.weather.weatherapp.city;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record FavoriteCityRequest(String username, String city) {
    @JsonCreator
    public FavoriteCityRequest(
            @JsonProperty("username") String username,
            @JsonProperty("city") String city
    ) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City cannot be null or empty");
        }
        this.username = username.trim();
        this.city = city.trim();
    }
}

