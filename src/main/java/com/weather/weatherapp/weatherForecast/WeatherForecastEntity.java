package com.weather.weatherapp.weatherForecast;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_forecast")
public class WeatherForecastEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(name = "city", nullable = false)
    private String city;
    @Column(name = "temperature", nullable = false)
    private float temperature;
    @Column(name = "description", nullable = false)
    private String description;
    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;
    @Column(name = "uv_index", nullable = false)
    private int uvIndex;
    @Column(name = "visibility", nullable = false)
    private int visibility;

    public WeatherForecastEntity() {
    }

    public WeatherForecastEntity( String city, float temperature, String description, LocalDateTime dateTime, int uvIndex, int visibility) {
        this.city = city;
        this.temperature = temperature;
        this.description = description;
        this.dateTime = dateTime;
        this.uvIndex = uvIndex;
        this.visibility = visibility;
    }


    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public int getUvIndex() {
        return uvIndex;
    }

    public void setUvIndex(int uvIndex) {
        this.uvIndex = uvIndex;
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }




}
