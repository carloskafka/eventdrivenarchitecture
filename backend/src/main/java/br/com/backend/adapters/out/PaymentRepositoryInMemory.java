package br.com.backend.adapters.out;

import br.com.backend.model.payment.Payment;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PaymentRepositoryInMemory implements PaymentRepository {

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
