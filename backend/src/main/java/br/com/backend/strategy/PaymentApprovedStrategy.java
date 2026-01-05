package br.com.backend.strategy;

import br.com.backend.model.payment.Payment;
import br.com.backend.adapters.out.PaymentRepository;
import br.com.backend.model.payment.PaymentStatus;
import br.com.libdomain.model.Event;
import br.com.libdomain.strategy.EventStrategy;

public class PaymentApprovedStrategy implements EventStrategy {

    private final PaymentRepository paymentRepository;

    public PaymentApprovedStrategy(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public boolean supports(Event event) {
        return "PAYMENT_APPROVED".equals(event.type());
    }

    @Override
    public void execute(Event event) {
        String paymentId = (String) event.payload().get("paymentId");

        // Retrieve aggregate
        Payment payment = paymentRepository.findById(paymentId)
                .orElseGet(() -> new Payment(paymentId, PaymentStatus.CREATED));

        // Apply event (idempotent + state transition)
        boolean applied = payment.applyEvent(event.eventId(), PaymentStatus.AUTHORIZED);

        if (applied) {
            paymentRepository.save(payment); // persist changes
            System.out.println("Payment approved: " + paymentId);
        } else {
            System.out.println("Event ignored (duplicate or invalid transition): " + paymentId);
        }
    }
}
