package com.weather.weatherapp.weatherForecast;

import com.weather.weatherapp.weatherForecast.Predictions.dto.TemperatureExtremes;
import com.weather.weatherapp.weatherForecast.Predictions.dto.TemperatureTrend;
import com.weather.weatherapp.weatherForecast.Predictions.dto.WeatherExtreme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherForecastRepository extends JpaRepository<WeatherForecastEntity, Long> {


    Optional<List<WeatherForecastEntity>> findByCityAndForecastTypeAndDateTimeBetween(
            String city,
            ForecastType forecastType,
            LocalDateTime startDate,
            LocalDateTime endDate);


// upitno

    @Query("SELECT w FROM WeatherForecastEntity w WHERE w.city = :city AND w.dateTime BETWEEN :startDate AND :endDate ORDER BY w.dateTime")
    List<WeatherForecastEntity> findHistoricalData(@Param("city") String city, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);



    @Query("SELECT w FROM WeatherForecastEntity w WHERE w.city = :city AND w.forecastType = :forecastType AND w.dateTime > :dateTime ORDER BY w.dateTime")
    List<WeatherForecastEntity> findFutureForecastsByCity(@Param("city") String city, @Param("forecastType") ForecastType forecastType, @Param("dateTime") LocalDateTime dateTime);


   @Query("SELECT new com.weather.weatherapp.weatherForecast.Predictions.dto.TemperatureTrend(DATE(w.dateTime), AVG(w.temperature)) " +
           "FROM WeatherForecastEntity w " +
           "WHERE w.city = :cityName AND w.dateTime BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(w.dateTime) " +
           "ORDER BY DATE(w.dateTime)")
   List<TemperatureTrend> getTemperatureTrend(
           @Param("cityName") String cityName,
           @Param("startDate") LocalDateTime startDate,
           @Param("endDate") LocalDateTime endDate
   );

    @Query("SELECT new com.weather.weatherapp.weatherForecast.Predictions.dto.TemperatureExtremes(" +
            "DATE(w.dateTime), MIN(w.temperature), MAX(w.temperature)) " +
            "FROM WeatherForecastEntity w " +
            "WHERE w.city = :cityName AND w.dateTime BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(w.dateTime) " +
            "ORDER BY DATE(w.dateTime)")
    List<TemperatureExtremes> getTemperatureExtremes(
            @Param("cityName") String cityName,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT new com.weather.weatherapp.weatherForecast.Predictions.dto.WeatherExtreme(MAX(w.temperature), MIN(w.temperature), " +
            "MAX(w.windSpeed), MAX(w.humidity)) " +
            "FROM WeatherForecastEntity w " +
            "WHERE w.city = :city AND w.dateTime BETWEEN :startDate AND :endDate")
    WeatherExtreme getWeatherExtremes(@Param("city") String city,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);




    @Query("SELECT AVG(w.temperature) FROM WeatherForecastEntity w " +
            "WHERE w.city = :cityName AND FUNCTION('MONTH', w.dateTime) = :month")
    Double getAverageTemperatureForMonth(
            @Param("cityName") String cityName,
            @Param("month") int month
    );

    Optional<WeatherForecastEntity> findTopByCityOrderByDateTimeDesc (String city);
}
