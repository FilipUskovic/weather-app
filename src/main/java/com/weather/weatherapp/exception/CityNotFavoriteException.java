package com.weather.weatherapp.exception;

public class CityNotFavoriteException extends RuntimeException {
    public CityNotFavoriteException(String message) {
        super(message);
    }
}
