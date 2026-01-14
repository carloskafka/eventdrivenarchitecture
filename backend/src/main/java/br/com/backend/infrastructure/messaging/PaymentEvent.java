package br.com.backend.infrastructure.messaging;

import br.com.backend.domain.payment.PaymentStatus;

import java.time.Instant;
import java.util.UUID;

public record PaymentEvent(
        UUID eventId,
        String paymentId,
        String orderId,
        PaymentStatus targetStatus,
        Instant occurredAt
) {}
