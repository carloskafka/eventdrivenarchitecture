package br.com.backend.adapters.out.persistence;

import br.com.backend.application.ports.out.PaymentRepository;
import br.com.backend.domain.payment.Payment;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryPaymentRepository implements PaymentRepository {

    private final Map<String, Payment> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Payment> findById(String paymentId) {
        return Optional.ofNullable(store.get(paymentId));
    }

    @Override
    public void save(Payment payment) {
        store.put(payment.getPaymentId(), payment);
    }
}
