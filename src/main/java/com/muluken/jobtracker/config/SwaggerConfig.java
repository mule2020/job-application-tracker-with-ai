package com.muluken.jobtracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .servers(List.of(
                        new Server()
                                .url("https://applytrackr.up.railway.app")
                                .description("Production server"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local server")
                ))
                .info(
                        new Info()
                                .title("Job Tracker API")
                                .version("1.0")
                                .description("Job Application Tracking System API")
                )
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(securitySchemeName)
                )
                .schemaRequirement(
                        securitySchemeName,
                        new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                );
    }

    @Configuration
    public static class RestClientConfig {

        @Bean
        public RestClient restClient() {

            var httpClient = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(10))
                    .version(java.net.http.HttpClient.Version.HTTP_1_1)
                    .build();

            return RestClient.builder()
                    .requestFactory(new org.springframework.http.client.JdkClientHttpRequestFactory(httpClient))
                    .build();
        }
    }
}