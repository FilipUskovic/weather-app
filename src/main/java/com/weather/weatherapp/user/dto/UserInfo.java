package com.weather.weatherapp.user.dto;

import java.util.List;

public record UserInfo(String username, List<String> roles) {
}
