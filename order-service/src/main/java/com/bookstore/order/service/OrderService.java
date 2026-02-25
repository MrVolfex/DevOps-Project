package com.bookstore.order.service;

import com.bookstore.order.config.RabbitMQConfig;
import com.bookstore.order.dto.OrderRequest;
import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.messaging.OrderCreatedEvent;
import com.bookstore.order.model.Order;
import com.bookstore.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;
    private final WebClient userServiceClient;
    private final WebClient bookServiceClient;

    public OrderService(OrderRepository orderRepository,
                        RabbitTemplate rabbitTemplate,
                        @Qualifier("userServiceClient") WebClient userServiceClient,
                        @Qualifier("bookServiceClient") WebClient bookServiceClient) {
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.userServiceClient = userServiceClient;
        this.bookServiceClient = bookServiceClient;
    }

    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creating order for userId={}, bookId={}, quantity={}",
                request.getUserId(), request.getBookId(), request.getQuantity());

        // REST komunikacija: validacija korisnika
        validateUser(request.getUserId());

        // REST komunikacija: dobavljanje podataka o knjizi
        Map<String, Object> bookData = getBook(request.getBookId());
        String bookTitle = (String) bookData.get("title");
        BigDecimal price = new BigDecimal(bookData.get("price").toString());
        Integer stock = (Integer) bookData.get("stock");

        if (stock < request.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + stock);
        }

        BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(request.getQuantity()));

        Order order = Order.builder()
                .userId(request.getUserId())
                .bookId(request.getBookId())
                .quantity(request.getQuantity())
                .totalPrice(totalPrice)
                .status(Order.OrderStatus.CONFIRMED)
                .build();

        Order saved = orderRepository.save(order);
        log.info("Order created with id: {}", saved.getId());


        updateStock(request.getBookId(), -request.getQuantity());

        // Message Queue: slanje dogadjaja Notification servisu
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(saved.getId())
                .userId(saved.getUserId())
                .bookId(saved.getBookId())
                .quantity(saved.getQuantity())
                .totalPrice(saved.getTotalPrice())
                .bookTitle(bookTitle)
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ORDER_CREATED_ROUTING_KEY,
                event
        );
        log.info("OrderCreatedEvent published to RabbitMQ for orderId: {}", saved.getId());

        return toResponse(saved);
    }

    public OrderResponse getOrderById(Long id) {
        log.info("Fetching order with id: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));
        return toResponse(order);
    }

    public List<OrderResponse> getOrdersByUser(Long userId) {
        log.info("Fetching orders for userId: {}", userId);
        return orderRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<OrderResponse> getAllOrders() {
        log.info("Fetching all orders");
        return orderRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateUser(Long userId) {
        log.info("Validating user with id: {} via REST", userId);
        try {
            userServiceClient.get()
                    .uri("/api/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getBook(Long bookId) {
        log.info("Fetching book with id: {} via REST", bookId);
        try {
            return bookServiceClient.get()
                    .uri("/api/books/{id}", bookId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            throw new IllegalArgumentException("Book not found with id: " + bookId);
        }
    }

    private void updateStock(Long bookId, int quantity) {
        log.info("Updating stock for bookId: {} by {} via REST", bookId, quantity);
        bookServiceClient.patch()
                .uri("/api/books/{id}/stock?quantity={q}", bookId, quantity)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    private OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .bookId(order.getBookId())
                .quantity(order.getQuantity())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
