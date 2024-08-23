package com.weather.weatherapp.exception;

public class DuplicateFavoriteCityException extends RuntimeException {
    public DuplicateFavoriteCityException(String message) {
        super(message);
    }
}
