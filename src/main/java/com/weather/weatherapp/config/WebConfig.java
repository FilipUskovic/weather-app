package com.weather.weatherapp.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, LocalDateTime.class, new FlexibleStringToLocalDateTimeConverter());
    }

    private static class FlexibleStringToLocalDateTimeConverter implements  Converter<String, LocalDateTime> {
        @Override

        public LocalDateTime convert( String source) {
            try {
                return LocalDateTime.parse(source, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e) {
                try {
                    LocalDate date = LocalDate.parse(source, DateTimeFormatter.ISO_LOCAL_DATE);
                    return date.atStartOfDay();
                } catch (DateTimeParseException e2) {
                    throw new IllegalArgumentException("Neispravan format datuma. Koristite 'YYYY-MM-DD' ili 'YYYY-MM-DDTHH:mm:ss'", e2);
                }
            }
        }
    }
}
