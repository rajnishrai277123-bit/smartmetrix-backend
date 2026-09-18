package com.smartmetrix.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI smartMetrixOpenAPI() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title("SmartMetrix API")
                                .description(
                                        "SmartMetrix - Research prototype " +
                                                "for OIML R-76-oriented testing, " +
                                                "inspection workflow, analytics, " +
                                                "drift detection and risk prediction."
                                )
                                .version("1.0.0")
                                .contact(
                                        new Contact()
                                                .name("SmartMetrix")
                                )
                );
    }
}