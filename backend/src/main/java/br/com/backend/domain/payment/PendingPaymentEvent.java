package br.com.backend.domain.payment;

import java.time.Instant;
import java.util.UUID;

public record PendingPaymentEvent(
        UUID eventId,
        PaymentStatus targetStatus,
        Instant occurredAt
) {}
