package br.com.backend.application.ports.out;

import br.com.backend.domain.payment.Payment;

import java.util.Optional;

public interface PaymentRepository {
    Optional<Payment> findById(String paymentId);
    void save(Payment payment);
}
