package br.com.backend.adapters.in;

import java.util.UUID;

/**
 * Simple DTO for payment events consumed from Kafka.
 */
public class PaymentEventDto {
    private UUID eventId;
    private String paymentId;
    private String status;

    public PaymentEventDto() {
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

