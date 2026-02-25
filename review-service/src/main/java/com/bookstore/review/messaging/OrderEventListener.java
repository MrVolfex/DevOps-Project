package com.bookstore.review.messaging;

import com.bookstore.review.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventListener {

    /**
     * RabbitMQ consumer - prima OrderCreatedEvent iz order-service.
     * Kada korisnik kupi knjigu, Review servis je obavešten
     * i može da omogući pisanje recenzije za tu kupljenu knjigu.
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent via RabbitMQ: orderId={}, userId={}, bookId={}, bookTitle={}",
                event.getOrderId(), event.getUserId(), event.getBookId(), event.getBookTitle());

        // Korisnik je kupio knjigu - sada može da ostavi recenziju
        log.info("User {} is now eligible to review book '{}' (bookId={})",
                event.getUserId(), event.getBookTitle(), event.getBookId());
    }
}
