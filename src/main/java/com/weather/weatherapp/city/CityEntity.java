package com.weather.weatherapp.city;

import com.weather.weatherapp.user.UserEntity;
import com.weather.weatherapp.weatherForecast.WeatherForecastEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "cities")
public class CityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "city", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WeatherForecastEntity> weatherForecasts = new ArrayList<>();

    @ManyToMany(mappedBy = "favoriteCities")
    private Set<UserEntity> users = new HashSet<>();

    public CityEntity() {
    }

    public CityEntity(String name) {
        this.name = name;
    }

    // metoda za pomoc upravljanja vezama
    public void addWeatherForecast(WeatherForecastEntity forecast) {
        weatherForecasts.add(forecast);
        forecast.setCity(String.valueOf(this));
    }

    public void removeWeatherForecast(WeatherForecastEntity forecast) {
        weatherForecasts.remove(forecast);
        forecast.setCity(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<WeatherForecastEntity> getWeatherForecasts() {
        return weatherForecasts;
    }

    public void setWeatherForecasts(List<WeatherForecastEntity> weatherForecasts) {
        this.weatherForecasts = weatherForecasts;
    }

    public Set<UserEntity> getUsers() {
        return users;
    }

    public void setUsers(Set<UserEntity> users) {
        this.users = users;
    }
}
