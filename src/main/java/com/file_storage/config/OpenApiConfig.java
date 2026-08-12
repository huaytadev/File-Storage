package com.file_storage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fileStorageOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("File Storage API")
                        .description("REST API for uploading, downloading and managing files using MinIO object storage.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Kevin Huayta")
                        )
                );
    }
}
