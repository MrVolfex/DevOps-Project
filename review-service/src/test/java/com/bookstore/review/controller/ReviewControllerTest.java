package com.bookstore.review.controller;

import com.bookstore.review.dto.ReviewRequest;
import com.bookstore.review.dto.ReviewResponse;
import com.bookstore.review.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest ne učitava AMQP bean-ove ni WebClient koji ReviewService gradi u konstruktoru.
// ReviewService je @MockBean — createReview se može testirati bez stvarnih spoljnih poziva.
@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    private ReviewResponse buildResponse() {
        return ReviewResponse.builder()
                .id(1L)
                .bookId(5L)
                .userId(10L)
                .rating(4)
                .comment("Odlična knjiga!")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private ReviewRequest buildRequest() {
        ReviewRequest req = new ReviewRequest();
        req.setBookId(5L);
        req.setUserId(10L);
        req.setRating(4);
        req.setComment("Odlična knjiga!");
        return req;
    }

    @Test
    void createReview_returns201() throws Exception {
        when(reviewService.createReview(any())).thenReturn(buildResponse());

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Odlična knjiga!"));
    }

    @Test
    void createReview_invalidBody_returns400() throws Exception {
        ReviewRequest invalid = new ReviewRequest(); // sva polja null/invalid

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllReviews_returns200() throws Exception {
        when(reviewService.getAllReviews()).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getReviewsByBook_returns200() throws Exception {
        when(reviewService.getReviewsByBook(5L)).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/reviews/book/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bookId").value(5));
    }

    @Test
    void getReviewsByUser_returns200() throws Exception {
        when(reviewService.getReviewsByUser(10L)).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/reviews/user/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(10));
    }

    @Test
    void getAverageRating_returns200() throws Exception {
        when(reviewService.getAverageRating(5L)).thenReturn(4.5);

        mockMvc.perform(get("/api/reviews/book/5/average-rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(4.5));
    }
}
