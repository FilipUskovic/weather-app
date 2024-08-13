package com.weather.weatherapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info().title("Weather app api")
                        .version("1.0.0")
                        .description("Weather app open api ui")
                        .termsOfService("https://your-terms-of-service.url")
                        .license(new License().name("API License").url("https://your-license-url")));


    }
}
