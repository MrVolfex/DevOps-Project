package com.bookstore.review.service;

import com.bookstore.review.dto.ReviewRequest;
import com.bookstore.review.dto.ReviewResponse;
import com.bookstore.review.model.Review;
import com.bookstore.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final WebClient bookServiceClient;

    public ReviewService(ReviewRepository reviewRepository,
                         @Value("${services.book-service.url:http://localhost:8082}") String bookServiceUrl) {
        this.reviewRepository = reviewRepository;
        this.bookServiceClient = WebClient.builder().baseUrl(bookServiceUrl).build();
    }

    public ReviewResponse createReview(ReviewRequest request) {
        log.info("Creating review for bookId={}, userId={}", request.getBookId(), request.getUserId());

        // REST komunikacija: validacija da knjiga postoji
        validateBook(request.getBookId());

        Review review = Review.builder()
                .bookId(request.getBookId())
                .userId(request.getUserId())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Review created with id: {}", saved.getId());
        return toResponse(saved);
    }

    public List<ReviewResponse> getReviewsByBook(Long bookId) {
        log.info("Fetching reviews for bookId: {}", bookId);
        return reviewRepository.findByBookId(bookId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ReviewResponse> getReviewsByUser(Long userId) {
        log.info("Fetching reviews for userId: {}", userId);
        return reviewRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public Double getAverageRating(Long bookId) {
        log.info("Calculating average rating for bookId: {}", bookId);
        Double avg = reviewRepository.findAverageRatingByBookId(bookId);
        return avg != null ? avg : 0.0;
    }

    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateBook(Long bookId) {
        log.info("Validating book with id: {} via REST", bookId);
        try {
            bookServiceClient.get()
                    .uri("/api/books/{id}", bookId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            throw new IllegalArgumentException("Book not found with id: " + bookId);
        }
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .bookId(review.getBookId())
                .userId(review.getUserId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
