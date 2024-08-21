package com.weather.weatherapp.auth;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;

public class LoggingDaoAuthenticationProvider extends DaoAuthenticationProvider {

    @Override
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        System.out.println("UserDetailsService set: " + userDetailsService);
        super.setUserDetailsService(userDetailsService);
    }
}
