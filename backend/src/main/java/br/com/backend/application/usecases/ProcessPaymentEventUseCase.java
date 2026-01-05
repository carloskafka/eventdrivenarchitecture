package br.com.backend.application.usecases;

import br.com.backend.adapters.out.PaymentRepository;
import br.com.backend.model.payment.Payment;
import br.com.backend.model.payment.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;
import jakarta.persistence.OptimisticLockException;

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

        try {
            repository.save(payment);
        } catch (RuntimeException e) {
            // Map repository-specific optimistic lock exceptions to Jakarta's OptimisticLockException
            if (isOptimisticLockException(e)) {
                throw new OptimisticLockException(e.getMessage());
            }
            throw e;
        }
    }

    private boolean isOptimisticLockException(Throwable t) {
        Throwable cur = t;
        while (cur != null) {
            String name = cur.getClass().getName();
            if (name.contains("OptimisticLock") || name.contains("OptimisticLockingFailureException")) {
                return true;
            }
            cur = cur.getCause();
        }
        return false;
    }

}
