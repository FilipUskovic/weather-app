package com.weather.weatherapp.email;

import com.weather.weatherapp.weatherForecast.WeatherForecastEntity;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender emailSender;

    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendWeatherAlert(String to, String username, String city, WeatherForecastEntity forecast) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        String htmlContent = String.format(
                "<h1>Weather Alert for %s</h1>" +
                        "<p>Dear %s,</p>" +
                        "<p>There is a significant weather change in %s.</p>" +
                        "<p>The current temperature is %.1f°C.</p>" +
                        "<p>Weather description: %s</p>" +
                        "<p>Humidity: %d%%</p>" +
                        "<p>Wind Speed: %.1f m/s</p>" +
                        "<p>Best regards,<br>Weather App Team</p>",
                city, username, city, forecast.getTemperature(), forecast.getDescription(),
                forecast.getHumidity(), forecast.getWindSpeed()
        );

        helper.setFrom("noreply@weatherapp.com");
        helper.setTo(to);
        helper.setSubject("Weather Alert for " + city);
        helper.setText(htmlContent, true);

        emailSender.send(message);
    }
}
