package com.devicemanagement.device_api.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI deviceApiOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Device Management API")
                .description("REST API for creating, updating, querying and deleting device "
                        + "resources, including domain rules for in-use devices.")
                .version("v1")
                .license(new License()
                        .name("MIT")
                        .url("https://opensource.org/licenses/MIT")));
    }
}
