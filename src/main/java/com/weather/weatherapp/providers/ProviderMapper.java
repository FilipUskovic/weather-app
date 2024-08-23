package com.weather.weatherapp.providers;

import com.weather.weatherapp.city.CityEntity;
import com.weather.weatherapp.weatherForecast.ForecastType;
import com.weather.weatherapp.weatherForecast.WeatherForecastEntity;
import com.weather.weatherapp.weatherForecast.dto.HistoricalDataEntry;
import com.weather.weatherapp.providers.dto.OpenMeteResponse;
import com.weather.weatherapp.weatherForecast.dto.WeatherResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class ProviderMapper {

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



    protected static List<WeatherForecastEntity> toHourlyWeatherForecasts(CityEntity city, OpenMeteResponse response) {
        return IntStream.range(0, response.hourly().time().size())
                .mapToObj(res -> {
                    int wmoCode = Integer.parseInt(response.hourly().apperiance().get(res)); // Dohvati WMO kod
                    String weatherDescription = ProviderMapper.getDescriptionForCode(wmoCode); // Pretvori u opis
                    return new WeatherForecastEntity(
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

    protected static List<WeatherForecastEntity> toDailyWeatherForecasts(CityEntity city, OpenMeteResponse response) {
        List<WeatherForecastEntity> forecasts = new ArrayList<>();
        if (response.daily() == null || response.daily().time() == null || response.daily().temperatureMax() == null) {
            throw new RuntimeException("Missing daily data for weather forecast.");
        }
        for (int i = 0; i < response.daily().time().size(); i++) {
            int wmoCode = Integer.parseInt(response.daily().description().get(i)); // Dohvati WMO kod
            String weatherDescription = ProviderMapper.getDescriptionForCode(wmoCode);
            WeatherForecastEntity forecast = new WeatherForecastEntity();
            forecast.setId(null);  // Eksplicitno postavljamo ID na null
            forecast.setCity(city.getName());
            forecast.setMaxTemperature(response.daily().temperatureMax().get(i).floatValue());
            forecast.setMinTemperature(response.daily().temperatureMin().get(i).floatValue());
            forecast.setDescription(weatherDescription);
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

    protected static WeatherForecastEntity toWeatherForecastFromOpenMeteo(CityEntity city, OpenMeteResponse response, int currentHourIndex) {
        WeatherForecastEntity forecast = new WeatherForecastEntity();
        forecast.setCity(city.getName());
        forecast.setTemperature(response.hourly().temperature2m().get(currentHourIndex).floatValue());
        forecast.setDescription(getDescriptionForCode(Integer.parseInt(response.hourly().apperiance().get(currentHourIndex))));
        forecast.setDateTime(LocalDateTime.parse(response.hourly().time().get(currentHourIndex)));
        forecast.setUvIndex(response.hourly().uvIndex().get(currentHourIndex).intValue());
        forecast.setVisibility(response.hourly().visibility().get(currentHourIndex));
        forecast.setHumidity(response.hourly().relativeHumidity2m().get(currentHourIndex));
        forecast.setPressure(response.hourly().pressureMsl().get(currentHourIndex).intValue());
        forecast.setFeelsLikeTemperature(response.hourly().apparentTemperature().get(currentHourIndex).floatValue());
        forecast.setWindSpeed(response.hourly().windspeed10m().get(currentHourIndex).floatValue());
        return forecast;
    }








    // open meteo weather za satne prognoze moram pretvoriti u codove koje imaju u dokumentaciji
    private static String getDescriptionForCode(int code) {
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


    public static List<WeatherForecastEntity> toHistoricalWeather(CityEntity cityEntity, HistoricalDataEntry historicalData) {
        List<WeatherForecastEntity> weatherForecasts = new ArrayList<>();
        for (int i = 0; i < historicalData.daily().time().size(); i++) {
            LocalDate date = LocalDate.parse(historicalData.daily().time().get(i));

            WeatherForecastEntity forecastEntity = new WeatherForecastEntity();
            forecastEntity.setCity(cityEntity.getName());
            forecastEntity.setDateTime(date.atStartOfDay());

            // Sigurno postavljanje podataka
            forecastEntity.setMaxTemperature(getFloatValueOrDefault(historicalData.daily().temperatureMax(), i, 0.0f));
            forecastEntity.setMinTemperature(getFloatValueOrDefault(historicalData.daily().temperatureMin(), i, 0.0f));
            forecastEntity.setFeelsLikeTemperature(getFloatValueOrDefault(historicalData.daily().feelsLikeMax(), i, 0.0f));
            forecastEntity.setDescription(String.valueOf(getValueOrDefault(historicalData.daily().weatherCode(), i, 0)));
            forecastEntity.setWindSpeed(getFloatValueOrDefault(historicalData.daily().windSpeedMax(), i, 0.0f));
           // forecastEntity.setUvIndex(getIntValueOrDefault(historicalData.daily().uvIndexMax(), i, 0));
            forecastEntity.setUvIndex(-1);
            forecastEntity.setForecastType(ForecastType.HISTORICAL);

            weatherForecasts.add(forecastEntity);
        }
        return weatherForecasts;
    }



    public static <T> T getValueOrDefault(List<T> list, int index, T defaultValue) {
        return (list != null && index < list.size() && list.get(index) != null) ? list.get(index) : defaultValue;
    }

    private static float getFloatValueOrDefault(List<Double> list, int index, float defaultValue) {
        if (list != null && index < list.size() && list.get(index) != null) {
            return list.get(index).floatValue();
        }
        return defaultValue;
    }

    private static int getIntValueOrDefault(List<Double> list, int index, int defaultValue) {
        if (list != null && index < list.size() && list.get(index) != null) {
            return list.get(index).intValue();
        }
        return defaultValue;
    }

}
