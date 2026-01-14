package br.com.backend.domain.payment;

import br.com.backend.domain.payment.events.PaymentApprovedDomainEvent;
import br.com.backend.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.*;

public class Payment {

    private final String paymentId;
    private final String orderId;
    private PaymentStatus status;

    private final Set<UUID> processedEventIds = new HashSet<>();
    private final List<PendingPaymentEvent> pendingEvents = new ArrayList<>();
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Payment(String paymentId, String orderId) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.status = PaymentStatus.CREATED;
    }

    public static Payment newPayment(String paymentId, String orderId) {
        return new Payment(paymentId, orderId);
    }

    public boolean isPending(UUID eventId) {
        return pendingEvents.stream().anyMatch(e -> e.eventId().equals(eventId));
    }


    public boolean applyEvent(UUID eventId, PaymentStatus targetStatus, Instant occurredAt) {

        if (processedEventIds.contains(eventId)) {
            return false;
        }

        if (status.canTransitionTo(targetStatus)) {
            transitionTo(eventId, targetStatus);
            reprocessPendingEvents();
            return true;
        }

        storeAsPending(eventId, targetStatus, occurredAt);
        return false;
    }

    private void transitionTo(UUID eventId, PaymentStatus targetStatus) {
        PaymentStatus previous = this.status;
        this.status = targetStatus;
        this.processedEventIds.add(eventId);

        if (previous != PaymentStatus.APPROVED && targetStatus == PaymentStatus.APPROVED) {
            domainEvents.add(new PaymentApprovedDomainEvent(paymentId, orderId));
        }
    }

    private void reprocessPendingEvents() {
        boolean changed;
        do {
            changed = false;
            pendingEvents.sort(Comparator.comparing(PendingPaymentEvent::occurredAt));
            Iterator<PendingPaymentEvent> it = pendingEvents.iterator();

            while (it.hasNext()) {
                PendingPaymentEvent pending = it.next();
                if (status.canTransitionTo(pending.targetStatus())) {
                    System.out.println("[UseCase] Reprocessando pendingEvent: " + pending.eventId() +
                            " status " + pending.targetStatus());
                    transitionTo(pending.eventId(), pending.targetStatus());
                    it.remove();
                    changed = true;
                    break;
                }
            }
        } while (changed);
    }


    private void storeAsPending(UUID eventId, PaymentStatus targetStatus, Instant occurredAt) {
        if (pendingEvents.stream().anyMatch(e -> e.eventId().equals(eventId))) {
            return;
        }
        pendingEvents.add(new PendingPaymentEvent(eventId, targetStatus, occurredAt));
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> copy = List.copyOf(domainEvents);
        domainEvents.clear();
        return copy;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public PaymentStatus getStatus() {
        return status;
    }
}
