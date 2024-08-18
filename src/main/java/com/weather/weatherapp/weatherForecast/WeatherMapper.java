package com.weather.weatherapp.weatherForecast;

import com.weather.weatherapp.city.CityEntity;
import com.weather.weatherapp.weatherForecast.dto.OpenMeteResponse;
import com.weather.weatherapp.weatherForecast.dto.WeatherResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WeatherMapper {

    public static WeatherForecastEntity toWeatherForecast(CityEntity city, WeatherResponse weather, float uvIndex) {
        WeatherForecastEntity forecast = new WeatherForecastEntity();
                forecast.setCity(city.getName());
                forecast.setTemperature(weather.temp().temp());
                forecast.setDescription(weather.descriptionDTO().getFirst().description());
                forecast.setDateTime(LocalDateTime.now());
                forecast.setUvIndex((int) uvIndex);
                forecast.setVisibility(weather.visibility());
                forecast.setHumidity( weather.temp().humidity());
                forecast.setPressure(weather.temp().pressure());
                forecast.setFeelsLikeTemperature(weather.temp().feelsLike());
               forecast.setWindSpeed(weather.wind().speed());
               return forecast;
    }

    public static List<WeatherForecastEntity> toHourlyWeatherForecasts(CityEntity city, OpenMeteResponse response) {
        return IntStream.range(0, response.hourly().time().size())
                .mapToObj(res -> {
                    int wmoCode = Integer.parseInt(response.hourly().apperiance().get(res)); // Dohvati WMO kod
                    String weatherDescription = WeatherMapper.getDescriptionForCode(wmoCode); // Pretvori u opis
                    return new WeatherForecastEntity(
                            null, // ID će se automatski generirati ako je autogeneriran u bazi podataka
                            city.getName(),
                            response.hourly().temperature2m().get(res).floatValue(),
                            weatherDescription,
                            LocalDateTime.parse(response.hourly().time().get(res)),
                            response.hourly().uvIndex().get(res).intValue(),
                            response.hourly().visibility().get(res),
                            response.hourly().relativeHumidity2m().get(res),
                            response.hourly().windspeed10m().get(res).floatValue(),
                            ForecastType.HOURLY,
                            response.hourly().apparentTemperature().get(res).floatValue(),
                            response.hourly().pressureMsl().get(res).intValue()
                    );
                }).collect(Collectors.toList());
    }



    public static List<WeatherForecastEntity> toDailyWeatherForecasts(CityEntity city, OpenMeteResponse response) {
        List<WeatherForecastEntity> forecasts = new ArrayList<>();

        if (response.daily() == null || response.daily().time() == null || response.daily().temperatureMax() == null) {
            throw new RuntimeException("Missing daily data for weather forecast.");
        }

        for (int i = 0; i < response.daily().time().size(); i++) {
            int wmoCode = Integer.parseInt(response.daily().description().get(i)); // Dohvati WMO kod
            String weatherDescription = WeatherMapper.getDescriptionForCode(wmoCode);
            WeatherForecastEntity forecast = new WeatherForecastEntity();
            forecast.setId(null);  // Eksplicitno postavljamo ID na null
            forecast.setCity(city.getName());
            forecast.setTemperature(response.daily().temperatureMax().get(i).floatValue());
            forecast.setDescription(weatherDescription);
         //   forecast.setMinTemperature(daily.temperatureMin().get(i).floatValue());
            forecast.setDateTime(LocalDate.parse(response.daily().time().get(i)).atStartOfDay());
            forecast.setUvIndex((int) Math.round(response.daily().uvIndexMax().get(i)));
            forecast.setWindSpeed(response.daily().windspeedMax().get(i).floatValue());
            forecast.setForecastType(ForecastType.DAILY);
            forecast.setFeelsLikeTemperature(response.daily().apparentTemperatureMax().get(i).floatValue());
            forecast.setHumidity(response.daily().relativeHumidityMax().get(i).intValue());
            forecasts.add(forecast);
        }
        return forecasts;
    }



// open meteo weather za satne prognoze moram pretvoriti u codove koje imaju u dokumentaciji
    public static String getDescriptionForCode(int code) {
        return switch (code) {
            case 0 -> "Clear sky";
            case 1  -> "Mainly clear";
            case 2 -> "partly cloudy";
            case 3 -> "overcast";
            case 45, 48 -> "Fog and depositing rime fog";
            case 51, 53, 55 -> "Drizzle: Light, moderate, and dense intensity";
            case 56, 57 -> "Freezing Drizzle: Light and dense intensity";
            case 61, 63, 65 -> "Rain: Slight, moderate and heavy intensity";
            case 66, 67 -> "Freezing Rain: Light and heavy intensity";
            case 71, 73, 75 -> "Snow fall: Slight, moderate, and heavy intensity";
            case 77 -> "Snow grains";
            case 80, 81, 82 -> "Rain showers: Slight, moderate, and violent";
            case 85, 86 -> "Snow showers: Slight and heavy";
            case 95 -> "Thunderstorm: Slight or moderate";
            case 96, 99 -> "Thunderstorm with slight and heavy hail";
            default -> "Unknown weather code";
        };
    }
}
