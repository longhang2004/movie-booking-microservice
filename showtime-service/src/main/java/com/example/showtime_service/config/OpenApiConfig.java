package com.example.showtime_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI showtimeServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Showtime Service API")
                        .description("REST API for managing showtimes in the Movie Booking System")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Movie Booking System")
                                .email("support@moviebooking.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
