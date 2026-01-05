package br.com.backend.support;

import br.com.libdomain.model.Event;

import java.util.Map;

/**
 * Factory class to create test events for various scenarios.
 */
public class TestEventFactory {

    public static Event orderCreated(String orderId) {
        return Event.of("ORDER_CREATED", Map.of("orderId", orderId));
    }

    public static Event paymentApproved(String paymentId) {
        return Event.of("PAYMENT_APPROVED", Map.of("paymentId", paymentId));
    }

    public static Event paymentFailed(String paymentId) {
        return Event.of("PAYMENT_FAILED", Map.of("paymentId", paymentId));
    }
}

