package br.com.backend.model.payment;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Payment {

    private final String paymentId;
    private PaymentStatus status;
    private final Set<String> processedEventIds = new HashSet<>();

    public Payment(String paymentId, PaymentStatus initialStatus) {
        this.paymentId = paymentId;
        this.status = initialStatus;
    }

    public synchronized boolean applyEvent(UUID eventId, PaymentStatus targetStatus) {
        // Idempotência
        if (processedEventIds.contains(eventId.toString())) {
            return false; // evento já processado
        }

        // Regra de transição de estado
        if (!status.canTransitionTo(targetStatus)) {
            return false; // transição inválida
        }

        // Aplica alteração
        this.status = targetStatus;
        processedEventIds.add(eventId.toString());
        return true;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getPaymentId() {
        return paymentId;
    }
}
