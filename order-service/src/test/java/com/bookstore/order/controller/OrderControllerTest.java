package com.bookstore.order.controller;

import com.bookstore.order.dto.OrderRequest;
import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.model.Order;
import com.bookstore.order.service.OrderService;
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

// @WebMvcTest ne učitava AMQP/RabbitMQ ni WebClient bean-ove — samo web sloj.
// OrderService je @MockBean pa nema potrebe za dodatnim mock-ovima infrastrukture.
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private OrderResponse buildResponse() {
        return OrderResponse.builder()
                .id(1L)
                .userId(10L)
                .bookId(5L)
                .quantity(2)
                .totalPrice(new BigDecimal("79.98"))
                .status(Order.OrderStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private OrderRequest buildRequest() {
        OrderRequest req = new OrderRequest();
        req.setUserId(10L);
        req.setBookId(5L);
        req.setQuantity(2);
        return req;
    }

    @Test
    void createOrder_returns201() throws Exception {
        when(orderService.createOrder(any())).thenReturn(buildResponse());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void createOrder_invalidBody_returns400() throws Exception {
        OrderRequest invalid = new OrderRequest(); // userId, bookId, quantity su null

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrderById_returns200() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    void getOrderById_notFound_returns400() throws Exception {
        when(orderService.getOrderById(99L))
                .thenThrow(new IllegalArgumentException("Order not found with id: 99"));

        mockMvc.perform(get("/api/orders/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Order not found with id: 99"));
    }

    @Test
    void getOrdersByUser_returns200() throws Exception {
        when(orderService.getOrdersByUser(10L)).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/orders/user/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAllOrders_returns200() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of(buildResponse(), buildResponse()));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
