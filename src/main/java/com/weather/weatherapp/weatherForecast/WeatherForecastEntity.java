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

    // razina 2
    @Column(name = "humidity", nullable = false)
    private int humidity;
    @Column(name = "wind_speed", nullable = false)
    private float windSpeed;
    @Enumerated(EnumType.STRING)
    private ForecastType forecastType;

    @Column(name = "feels_like_temp", nullable = false)
    private float feelsLikeTemperature;

    @Column(name = "pressure", nullable = false)
    private int pressure;


    public WeatherForecastEntity() {
    }

    // Konstruktor za mapper
    public WeatherForecastEntity(String city, float temperature, String description, LocalDateTime dateTime, int uvIndex, int visibility,
                                 int humidity, int pressure, float feelsLike, float windSpeed) {
        this.city = city;
        this.temperature = temperature;
        this.description = description;
        this.dateTime = dateTime;
        this.uvIndex = uvIndex;
        this.visibility = visibility;
        this.forecastType = ForecastType.CURRENT; // Postavljamo default vrijednost
        this.humidity = humidity;
        this.pressure = pressure;
        this.feelsLikeTemperature = feelsLike;
        this.windSpeed = windSpeed;
    }

    public WeatherForecastEntity(Long id, String city, float temperature, String description, LocalDateTime dateTime, int uvIndex,
                                 int visibility, int humidity, float windSpeed, ForecastType forecastType, float feelsLikeTemperature, int pressure) {
        Id = id;
        this.city = city;
        this.temperature = temperature;
        this.description = description;
        this.dateTime = dateTime;
        this.uvIndex = uvIndex;
        this.visibility = visibility;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.forecastType = forecastType;
        this.feelsLikeTemperature = feelsLikeTemperature;
        this.pressure = pressure;
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

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public float getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(float windSpeed) {
        this.windSpeed = windSpeed;
    }

    public ForecastType getForecastType() {
        return forecastType;
    }

    public void setForecastType(ForecastType forecastType) {
        this.forecastType = forecastType;
    }

    public float getFeelsLikeTemperature() {
        return feelsLikeTemperature;
    }

    public void setFeelsLikeTemperature(float feelsLikeTemperature) {
        this.feelsLikeTemperature = feelsLikeTemperature;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }
}
