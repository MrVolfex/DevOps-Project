package com.bookstore.review.service;

import com.bookstore.review.dto.ReviewResponse;
import com.bookstore.review.model.Review;
import com.bookstore.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

// ReviewService gradi WebClient sam u konstruktoru iz URL stringa (@Value).
// createReview() poziva validateBook() koji koristi WebClient — ne testiramo na unit nivou.
// Testiramo samo metode koje čitaju isključivo iz ReviewRepository (bez spoljnih poziva).
// createReview je pokriven na controller nivou pomoću @MockBean.
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    private ReviewService reviewService;

    // Direktna konstrukcija — prosleđujemo URL string umesto @Value injekcije
    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewRepository, "http://localhost:8082");
    }

    private Review buildReview() {
        return Review.builder()
                .id(1L)
                .bookId(5L)
                .userId(10L)
                .rating(4)
                .comment("Odlična knjiga!")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getReviewsByBook_success() {
        when(reviewRepository.findByBookId(5L)).thenReturn(List.of(buildReview(), buildReview()));

        List<ReviewResponse> result = reviewService.getReviewsByBook(5L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getBookId()).isEqualTo(5L);
    }

    @Test
    void getReviewsByUser_success() {
        when(reviewRepository.findByUserId(10L)).thenReturn(List.of(buildReview()));

        List<ReviewResponse> result = reviewService.getReviewsByUser(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(10L);
    }

    @Test
    void getAverageRating_success() {
        when(reviewRepository.findAverageRatingByBookId(5L)).thenReturn(4.5);

        Double avg = reviewService.getAverageRating(5L);

        assertThat(avg).isEqualTo(4.5);
    }

    @Test
    void getAverageRating_noReviews_returnsZero() {
        // Kada nema recenzija, @Query vraća null — servis treba da vrati 0.0
        when(reviewRepository.findAverageRatingByBookId(99L)).thenReturn(null);

        Double avg = reviewService.getAverageRating(99L);

        assertThat(avg).isEqualTo(0.0);
    }

    @Test
    void getAllReviews_success() {
        when(reviewRepository.findAll()).thenReturn(List.of(buildReview(), buildReview(), buildReview()));

        List<ReviewResponse> result = reviewService.getAllReviews();

        assertThat(result).hasSize(3);
    }
}
