package com.cathayunitedbank.currencyconverter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

/**
 * Configuration class for OpenAPI (Swagger UI) documentation.
 * This class defines the custom settings for the OpenAPI specification,
 * which generates interactive API documentation for the Currency Converter application.
 * It provides metadata about the API, contact information, licensing details,
 * and server URLs where the API can be accessed.
 *
 * Key Attributes:
 * - **API Info**: Sets the title, description, and version of the API.
 * - **Contact Information**: Specifies the name, URL, and email for the API's contact.
 * - **License Information**: Provides details about the API's license.
 * - **Servers**: Defines the available server environments (e.g., development)
 * where the API is hosted, allowing users to easily switch between them in the Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures and provides the custom OpenAPI bean for the Currency Converter API.
     * This bean is used by Springdoc to generate the Swagger UI documentation.
     *
     * @return A fully configured {@link OpenAPI} object with API details and server information.
     */
    @Bean
    public OpenAPI currencyConverterOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Currency Converter API")
                        .description("API for currency conversion and exchange rate management")
                        .version("v0.0.1")
                        .contact(new Contact()
                                .name("Cathay United Bank")
                                .url("#")
                                .email("api@cathayunitedbank.com"))
                        .license(new License()
                                .name("API License")
                                .url("#")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server")
                ));
    }
}