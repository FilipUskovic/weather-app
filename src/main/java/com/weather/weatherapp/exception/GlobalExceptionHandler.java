package com.weather.weatherapp.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@ControllerAdvice
public class GlobalExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(WeatherServiceException.class)
    public ResponseEntity<String> handleHttpClientErrorException(WeatherServiceException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        logger.error("Failed to read HTTP message", ex);
        return ResponseEntity.badRequest().body("Invalid request body: " + ex.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        if (ex.getRequiredType() != null && LocalDateTime.class.isAssignableFrom(ex.getRequiredType())) {
            String dateString = (String) ex.getValue();
            try {
                // Pokušaj parsirati kao LocalDateTime
                LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e) {
                try {
                    // Ako ne uspije, pokušaj parsirati kao LocalDate i konvertirati u LocalDateTime
                    LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
                    LocalDateTime dateTime = date.atStartOfDay();
                    return new ResponseEntity<>("Datum je uspješno parsiran kao: " + dateTime, HttpStatus.OK);
                } catch (DateTimeParseException e2) {
                    return new ResponseEntity<>("Neispravan format datuma. Koristite 'YYYY-MM-DD' ili 'YYYY-MM-DDTHH:mm:ss'", HttpStatus.BAD_REQUEST);
                }
            }
        }

        String name = ex.getName();
        String type = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "nepoznat tip";
        Object value = ex.getValue();
        String message = String.format("'%s' bi trebao biti validan %s, a '%s' to nije", name, type, value);

        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }
}
