package com.weather.weatherapp.weatherForecast;

import com.weather.weatherapp.weatherForecast.dto.HistoricalDataEntry;
import com.weather.weatherapp.weatherForecast.dto.PredictionDataEntry;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "weather_forecast")
public class WeatherForecastEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(name = "city", nullable = false)
    private String city;
    @Column(nullable = false)
    private float temperature;

    private String description;
    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    private int uvIndex;

    private int visibility;
    // razina 2
    private int humidity;
    private float windSpeed;
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ForecastType forecastType;

    @Column(name = "feels_like")
    private float feelsLikeTemperature;

    private int pressure;

    // razina 3
    @Column(name = "min_temp")
    private float minTemperature;

    @Column(name = "max_temp")
    private float maxTemperature;

    @ElementCollection
    @CollectionTable(name = "historical_data", joinColumns = @JoinColumn(name = "forecast_id"))
    private List<HistoricalDataEntry> historicalData = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "prediction_data", joinColumns = @JoinColumn(name = "forecast_id"))
    private List<PredictionDataEntry> predictionData = new ArrayList<>();



    public WeatherForecastEntity() {
    }

    // Konstruktor za mapper
    // TODO razmotriti o builder-u
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

    public WeatherForecastEntity(String city, float temperature, String description, LocalDateTime dateTime, int uvIndex,
                                 int visibility, int humidity, float windSpeed, ForecastType forecastType, float feelsLike, int pressure) {
            this.city = city;
            this.temperature = temperature;
            this.description = description;
            this.dateTime = dateTime;
            this.uvIndex = uvIndex;
            this.visibility = visibility;
            this.humidity = humidity;
            this.forecastType = forecastType; // Postavljamo default vrijednost
            this.windSpeed = windSpeed;
            this.feelsLikeTemperature = feelsLike;
            this.pressure = pressure;

    }


    // upravljanje kolekcijama
    public void addHistoricalData(HistoricalDataEntry entry) {
        this.historicalData.add(entry);
    }

    public void addPredictionData(PredictionDataEntry entry) {
        this.predictionData.add(entry);
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

    public float getMinTemperature() {
        return minTemperature;
    }

    public void setMinTemperature(float minTemperature) {
        this.minTemperature = minTemperature;
    }

    public float getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(float maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public List<HistoricalDataEntry> getHistoricalData() {
        return historicalData;
    }

    public void setHistoricalData(List<HistoricalDataEntry> historicalData) {
        this.historicalData = historicalData;
    }

    public List<PredictionDataEntry> getPredictionData() {
        return predictionData;
    }

    public void setPredictionData(List<PredictionDataEntry> predictionData) {
        this.predictionData = predictionData;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeatherForecastEntity that = (WeatherForecastEntity) o;
        return Float.compare(that.temperature, temperature) == 0 &&
                Objects.equals(Id, that.Id) &&
                Objects.equals(city, that.city) &&
                Objects.equals(dateTime, that.dateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Id, city, temperature, dateTime);
    }

    @Override
    public String toString() {
        return "WeatherForecastEntity{" +
                "id=" + Id +
                ", city='" + city + '\'' +
                ", temperature=" + temperature +
                ", dateTime=" + dateTime +
                ", forecastType=" + forecastType +
                '}';
    }
}
