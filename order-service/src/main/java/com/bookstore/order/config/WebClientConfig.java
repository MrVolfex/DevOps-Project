package com.bookstore.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.user-service.url:http://localhost:8081}")
    private String userServiceUrl;

    @Value("${services.book-service.url:http://localhost:8082}")
    private String bookServiceUrl;

    @Bean("userServiceClient")
    public WebClient userServiceClient() {
        return WebClient.builder()
                .baseUrl(userServiceUrl)
                .build();
    }

    @Bean("bookServiceClient")
    public WebClient bookServiceClient() {
        return WebClient.builder()
                .baseUrl(bookServiceUrl)
                .build();
    }
}
