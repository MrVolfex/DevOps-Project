package com.bookstore.book.controller;

import com.bookstore.book.dto.BookRequest;
import com.bookstore.book.dto.BookResponse;
import com.bookstore.book.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    private BookResponse buildResponse() {
        return BookResponse.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert Martin")
                .isbn("978-0132350884")
                .price(new BigDecimal("39.99"))
                .stock(10)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private BookRequest buildRequest() {
        BookRequest req = new BookRequest();
        req.setTitle("Clean Code");
        req.setAuthor("Robert Martin");
        req.setPrice(new BigDecimal("39.99"));
        req.setStock(5);
        return req;
    }

    @Test
    void createBook_returns201() throws Exception {
        when(bookService.createBook(any())).thenReturn(buildResponse());

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.author").value("Robert Martin"));
    }

    @Test
    void createBook_invalidBody_returns400() throws Exception {
        BookRequest invalid = new BookRequest(); // title i author su null — pada validacija

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookById_returns200() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    void getBookById_notFound_returns400() throws Exception {
        when(bookService.getBookById(99L))
                .thenThrow(new IllegalArgumentException("Book not found with id: 99"));

        mockMvc.perform(get("/api/books/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Book not found with id: 99"));
    }

    @Test
    void getAllBooks_returns200() throws Exception {
        when(bookService.getAllBooks()).thenReturn(List.of(buildResponse(), buildResponse()));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void searchBooks_byTitle_returns200() throws Exception {
        when(bookService.searchBooks("Clean", null)).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/books/search").param("title", "Clean"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Clean Code"));
    }

    @Test
    void deleteBook_returns204() throws Exception {
        doNothing().when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());
    }
}
