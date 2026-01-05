package br.com.backend.adapters.out;

import br.com.backend.model.payment.Payment;
import br.com.libdomain.ports.RepositoryPort;

/**
 * Repository for the Payment aggregate.
 * Implements the generic RepositoryPort interface for CRUD operations.
 */
public interface PaymentRepository extends RepositoryPort<Payment, String> {
    // Domain-specific methods can be added here if needed
    // e.g. findByStatus(PaymentStatus status)
}
