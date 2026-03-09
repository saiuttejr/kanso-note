package com.bankingoop.finance.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration — auto-generated API documentation.
 */
@Configuration
public class OpenApiConfig {

    /** Creates OpenAPI/Swagger documentation bean for auto-generated API docs. */
    @Bean
    public OpenAPI kansoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Kanso Finance API")
                        .description("RESTful API for Kanso Personal Finance Tracker. " +
                                "Provides endpoints for transaction management, budget tracking, " +
                                "analytics, and audit logging.")
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("Sai Uttej R"))
                        .license(new License()
                                .name("MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development")));
    }
}
