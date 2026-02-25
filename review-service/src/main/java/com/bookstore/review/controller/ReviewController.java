package com.bookstore.review.controller;

import com.bookstore.review.dto.ReviewRequest;
import com.bookstore.review.dto.ReviewResponse;
import com.bookstore.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest request) {
        log.info("POST /api/reviews");
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(request));
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getAllReviews() {
        log.info("GET /api/reviews");
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByBook(@PathVariable Long bookId) {
        log.info("GET /api/reviews/book/{}", bookId);
        return ResponseEntity.ok(reviewService.getReviewsByBook(bookId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByUser(@PathVariable Long userId) {
        log.info("GET /api/reviews/user/{}", userId);
        return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
    }

    @GetMapping("/book/{bookId}/average-rating")
    public ResponseEntity<Map<String, Double>> getAverageRating(@PathVariable Long bookId) {
        log.info("GET /api/reviews/book/{}/average-rating", bookId);
        return ResponseEntity.ok(Map.of("averageRating", reviewService.getAverageRating(bookId)));
    }
}
