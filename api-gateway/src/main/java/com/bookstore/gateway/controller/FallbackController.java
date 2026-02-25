package com.bookstore.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    @GetMapping("/users")
    public ResponseEntity<Map<String, String>> usersFallback() {
        log.warn("User service is unavailable - returning fallback response");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "User service is currently unavailable. Please try again later."));
    }

    @GetMapping("/books")
    public ResponseEntity<Map<String, String>> booksFallback() {
        log.warn("Book service is unavailable - returning fallback response");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Book service is currently unavailable. Please try again later."));
    }

    @GetMapping("/orders")
    public ResponseEntity<Map<String, String>> ordersFallback() {
        log.warn("Order service is unavailable - returning fallback response");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Order service is currently unavailable. Please try again later."));
    }

    @GetMapping("/reviews")
    public ResponseEntity<Map<String, String>> reviewsFallback() {
        log.warn("Review service is unavailable - returning fallback response");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Review service is currently unavailable. Please try again later."));
    }
}
