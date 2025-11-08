package com.maxzdosreis.products_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product API")
                            .version("1.0")
                            .description("API Documentation")
                            .license(new License()
                                .name("MIT License")
                                .url("https://mit-license.org/")
                        )
                );
    }
}
