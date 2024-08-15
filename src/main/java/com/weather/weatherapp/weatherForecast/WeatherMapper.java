package com.weather.weatherapp.weatherForecast;

import com.weather.weatherapp.weatherForecast.dto.OpenMeteResponse;
import com.weather.weatherapp.weatherForecast.dto.WeatherResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WeatherMapper {

    public static WeatherForecastEntity toWeatherForecast(String city, WeatherResponse weather, float uvIndex) {
        return new WeatherForecastEntity(
                city,
                weather.temp().temp(),
                weather.descriptionDTO().getFirst().description(),
                LocalDateTime.now(),
                (int) uvIndex,
                weather.visibility(),
                weather.temp().humidity(),
                weather.temp().pressure(),
                weather.temp().feelsLike(),
                weather.wind().speed()
        );
    }

    public static List<WeatherForecastEntity> toHourlyWeatherForecasts(String city, OpenMeteResponse response){
        return IntStream.range(0, response.hourly().time().size())
                .mapToObj(res -> new WeatherForecastEntity(
                        null,
                        city,
                        response.hourly().temperature2m().get(res).floatValue(),
                        "N/A",
                        LocalDateTime.parse(response.hourly().time().get(res)),
                        response.hourly().uvIndex().get(res).intValue(),
                        response.hourly().visibility().get(res),
                        response.hourly().relativeHumidity2m().get(res),
                        response.hourly().windspeed10m().get(res).floatValue(),
                        ForecastType.HOURLY,
                        response.hourly().apparentTemperature().get(res).floatValue(),
                        response.hourly().pressureMsl().get(res).intValue()
                )).collect(Collectors.toList());
    }

    public static List<WeatherForecastEntity> toDailyWeatherForecasts(String city, OpenMeteResponse response) {
        List<WeatherForecastEntity> forecasts = new ArrayList<>();

        if (response.daily() == null || response.daily().time() == null || response.daily().temperatureMax() == null) {
            throw new RuntimeException("Missing daily data for weather forecast.");
        }

        for (int i = 0; i < response.daily().time().size(); i++) {
            WeatherForecastEntity forecast = new WeatherForecastEntity();
            forecast.setId(null);  // Eksplicitno postavljamo ID na null
            forecast.setCity(city);
            forecast.setTemperature(response.daily().temperatureMax().get(i).floatValue());
         //   forecast.setMinTemperature(daily.temperatureMin().get(i).floatValue());
            forecast.setDateTime(LocalDate.parse(response.daily().time().get(i)).atStartOfDay());
            forecast.setUvIndex((int) Math.round(response.daily().uvIndexMax().get(i)));
            forecast.setWindSpeed(response.daily().windspeedMax().get(i).floatValue());
            forecast.setForecastType(ForecastType.DAILY);
            forecast.setFeelsLikeTemperature(response.daily().apparentTemperatureMax().get(i).floatValue());
           // forecast.setPrecipitation(daily.precipitationSum().get(i).floatValue());
         //   forecast.setPressure(daily.pressure().get(i).intValue());
            // PROMJENA: Postavljanje opisa i ostalih polja koja možda nedostaju u dnevnoj prognozi
            forecast.setDescription("Daily forecast");
            forecasts.add(forecast);
        }
        return forecasts;
    }

}
