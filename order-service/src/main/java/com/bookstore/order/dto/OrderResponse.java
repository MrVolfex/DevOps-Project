package com.bookstore.order.dto;

import com.bookstore.order.model.Order;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private Long userId;
    private Long bookId;
    private Integer quantity;
    private BigDecimal totalPrice;
    private Order.OrderStatus status;
    private LocalDateTime createdAt;
}
