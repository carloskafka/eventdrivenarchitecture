package br.com.backend.adapters.out;

import br.com.backend.model.payment.Payment;
import jakarta.persistence.OptimisticLockException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementação em memória do repositório de pagamentos.
 * Utiliza um ConcurrentHashMap para armazenar os pagamentos,
 * permitindo acesso concorrente seguro.
 */
@Component
public class PaymentRepositoryInMemory implements PaymentRepository {

    private final Map<String, Payment> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Payment> findById(String paymentId) {
        Payment payment = store.get(paymentId);
        return payment == null
                ? Optional.empty()
                : Optional.of(payment.copy());
    }

    @Override
    public void save(Payment payment) {
        store.compute(payment.getPaymentId(), (id, current) -> {

            if (current != null && current.getVersion() != payment.getVersion()) {
                throw new OptimisticLockException(
                        "Version conflict for payment " + id
                );
            }

            Payment toPersist = payment.copy();
            toPersist.incrementVersion();
            return toPersist;
        });
    }
}
