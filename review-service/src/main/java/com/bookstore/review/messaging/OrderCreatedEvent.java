package com.bookstore.review.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private Long orderId;
    private Long userId;
    private Long bookId;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String bookTitle;
}
