package com.ss.quartzScheduler.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger configuration for API documentation using OpenAPI 3.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Quartz Job Management API")
                        .description("REST API for managing Quartz scheduled jobs with suspension and triggering capabilities")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Development Team")
                                .email("sahil.salunke66666@gmail.com")
                                .url("https://github.com/sahil-salunke6/quartzSchedular"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:9091")
                                .description("Development server"),
                        new Server()
                                .url("https://api.quartzpoc.com")
                                .description("Production server")
                ));
    }
}