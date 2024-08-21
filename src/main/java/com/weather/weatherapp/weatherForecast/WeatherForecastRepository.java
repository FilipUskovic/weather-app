package com.weather.weatherapp.weatherForecast;

import com.weather.weatherapp.weatherForecast.Predictions.dto.TemperatureExtremes;
import com.weather.weatherapp.weatherForecast.Predictions.dto.TemperatureTrend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherForecastRepository extends JpaRepository<WeatherForecastEntity, Long> {

    List<WeatherForecastEntity> findByCity(String city);

    Optional<List<WeatherForecastEntity>> findByCityAndForecastTypeAndDateTimeBetween(
            String city,
            ForecastType forecastType,
            LocalDateTime startDate,
            LocalDateTime endDate);

    Optional<WeatherForecastEntity> findFirstByCityAndForecastTypeOrderByDateTimeDesc(String city, ForecastType forecastType);

    Optional <WeatherForecastEntity> findByCityAndForecastType(String cities, ForecastType forecastType);

// upitno
    @Query("SELECT w FROM WeatherForecastEntity w WHERE w.city = :city AND w.dateTime BETWEEN :startDate AND :endDate ORDER BY w.dateTime")
    List<WeatherForecastEntity> findHistoricalData(@Param("city") String city, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT AVG(w.temperature) FROM WeatherForecastEntity w WHERE w.city = :city AND w.dateTime BETWEEN :startDate AND :endDate")
    Double findAverageTemperature(@Param("city") String city, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT w FROM WeatherForecastEntity w WHERE w.city = :city AND w.forecastType = :forecastType AND w.dateTime > :dateTime ORDER BY w.dateTime")
    List<WeatherForecastEntity> findFutureForecastsByCity(@Param("city") String city, @Param("forecastType") ForecastType forecastType, @Param("dateTime") LocalDateTime dateTime);

   // List<String> findDistinctByCity();
/*
    Query("SELECT NEW com.weather.weatherapp.dto.TemperatureStatistics(AVG(w.temperature), MAX(w.maxTemperature), MIN(w.minTemperature)) " +
                  "FROM WeatherForecastEntity w " +
                  "WHERE w.city.name = :cityName AND w.dateTime BETWEEN :startDate AND :endDate")
    TemperatureStatistics getTemperatureStatistics(@Param("cityName") String cityName,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT NEW com.weather.weatherapp.dto.TemperatureTrend(DATE(w.dateTime) as date, AVG(w.temperature) as avgTemp) " +
            "FROM WeatherForecastEntity w " +
            "WHERE w.city.name = :cityName AND w.dateTime BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(w.dateTime) " +
            "ORDER BY DATE(w.dateTime)")
    List<TemperatureTrend> getTemperatureTrend(@Param("cityName") String cityName,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT h FROM WeatherForecastEntity w JOIN w.historicalData h " +
            "WHERE w.city.name = :cityName AND h.dateTime BETWEEN :startDate AND :endDate " +
            "ORDER BY h.dateTime")
    List<HistoricalDataEntry> getHistoricalData(@Param("cityName") String cityName,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM WeatherForecastEntity w JOIN w.predictionData p " +
            "WHERE w.city.name = :cityName AND p.predictionDateTime > :startDate " +
            "ORDER BY p.predictionDateTime")
    List<PredictionDataEntry> getFuturePredictions(@Param("cityName") String cityName,
                                                   @Param("startDate") LocalDateTime startDate);


 */
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
            "DATE(w.dateTime), MIN(w.minTemperature), MAX(w.maxTemperature)) " +
            "FROM WeatherForecastEntity w " +
            "WHERE w.city = :cityName AND w.dateTime BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(w.dateTime) " +
            "ORDER BY DATE(w.dateTime)")
    List<TemperatureExtremes> getTemperatureExtremes(
            @Param("cityName") String cityName,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT AVG(w.temperature) FROM WeatherForecastEntity w " +
            "WHERE w.city = :cityName AND FUNCTION('MONTH', w.dateTime) = :month")
    Double getAverageTemperatureForMonth(
            @Param("cityName") String cityName,
            @Param("month") int month
    );

    //findTopByCityOrderByDateTimeDesc
    Optional<WeatherForecastEntity> findTopByCityOrderByDateTimeDesc (String city);
}
