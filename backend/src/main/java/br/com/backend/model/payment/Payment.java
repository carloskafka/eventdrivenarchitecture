package br.com.backend.model.payment;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Payment entity with business logic to apply events in an idempotent manner
 * and validate state transitions.
 */
public class Payment {

    private final String paymentId;
    private PaymentStatus status;
    private long version;

    private final Set<String> processedEventIds = new HashSet<>();

    public Payment(String paymentId, PaymentStatus initialStatus) {
        this.paymentId = paymentId;
        this.status = initialStatus;
        this.version = 0;
    }

    /**
     * Applies an event idempotently and validates the state transition.
     * NOTE: this method does NOT handle concurrency control.
     */
    public boolean applyEvent(UUID eventId, PaymentStatus targetStatus) {

        // Idempotency
        if (processedEventIds.contains(eventId.toString())) {
            return false;
        }

        // State transition rule
        if (!status.canTransitionTo(targetStatus)) {
            return false;
        }

        // Apply change
        this.status = targetStatus;
        this.processedEventIds.add(eventId.toString());
        return true;
    }

    /* ========= getters ========= */

    public String getPaymentId() {
        return paymentId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public long getVersion() {
        return version;
    }

    /* ========= infrastructure methods ========= */

    public void incrementVersion() {
        this.version++;
    }

    /**
     * Used by the repository to ensure isolation.
     */
    public Set<String> getProcessedEventIds() {
        return new HashSet<>(processedEventIds);
    }

    /**
     * Copy factory to simulate detach (as JPA would do).
     */
    public Payment copy() {
        Payment copy = new Payment(this.paymentId, this.status);
        copy.version = this.version;
        copy.processedEventIds.addAll(this.processedEventIds);
        return copy;
    }
}
