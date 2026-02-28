package com.bookstore.order.service;

import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.model.Order;
import com.bookstore.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

// createOrder() poziva WebClient (spoljni REST poziv) — ne testiramo na unit nivou.
// Testiramo samo metode koje ne zavise od spoljnih servisa.
// createOrder je pokriven na controller nivou pomoću @MockBean.
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    // Potrebni su za konstruktor, ali se ne koriste u testiranim metodama
    @Mock
    private WebClient userServiceClient;

    @Mock
    private WebClient bookServiceClient;

    private OrderService orderService;

    // Ručna konstrukcija jer konstruktor prima @Qualifier WebClient parametre
    // koje @InjectMocks ne može jednoznačno da razreši (isti tip, dva bina)
    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, rabbitTemplate, userServiceClient, bookServiceClient);
    }

    private Order buildOrder() {
        return Order.builder()
                .id(1L)
                .userId(10L)
                .bookId(5L)
                .quantity(2)
                .totalPrice(new BigDecimal("79.98"))
                .status(Order.OrderStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getOrderById_success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(buildOrder()));

        OrderResponse response = orderService.getOrderById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);
    }

    @Test
    void getOrderById_notFound_throwsException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void getOrdersByUser_success() {
        when(orderRepository.findByUserId(10L)).thenReturn(List.of(buildOrder(), buildOrder()));

        List<OrderResponse> result = orderService.getOrdersByUser(10L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUserId()).isEqualTo(10L);
    }

    @Test
    void getAllOrders_success() {
        when(orderRepository.findAll()).thenReturn(List.of(buildOrder()));

        List<OrderResponse> result = orderService.getAllOrders();

        assertThat(result).hasSize(1);
    }
}
