package br.com.backend.application.usecases;

import br.com.backend.adapters.out.PaymentRepository;
import br.com.backend.model.payment.Payment;
import br.com.backend.model.payment.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Use case for processing payment events in an idempotent manner.
 * It ensures that the same event is not applied multiple times to a payment.
 */
@Component
public class ProcessPaymentEventUseCase {

    private final PaymentRepository repository;

    public ProcessPaymentEventUseCase(PaymentRepository repository) {
        this.repository = repository;
    }

    public void execute(UUID eventId, String paymentId, PaymentStatus targetStatus) {

        Payment payment = repository.findById(paymentId)
                .orElseGet(() -> new Payment(
                        paymentId,
                        PaymentStatus.CREATED
                ));

        boolean applied = payment.applyEvent(eventId, targetStatus);

        if (!applied) {
            System.out.println(Thread.currentThread().getName() + " NO-OP (IDEMPOTENT)");
            return;
        }

        repository.save(payment);
    }

}
