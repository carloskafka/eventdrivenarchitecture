package br.com.backend.domain.payment.events;

import br.com.backend.domain.shared.DomainEvent;

import java.time.Instant;

public record PaymentApprovedDomainEvent(
        String paymentId,
        String orderId,
        Instant occurredAt
) implements DomainEvent {

    public PaymentApprovedDomainEvent(String paymentId, String orderId) {
        this(paymentId, orderId, Instant.now());
    }
}
