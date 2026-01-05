package br.com.backend.adapters.out;

import br.com.backend.model.payment.Payment;
import br.com.libdomain.ports.RepositoryPort;
import org.springframework.stereotype.Repository;

/**
 * Repositório para a entidade Payment.
 * Implementa a interface genérica RepositoryPort para operações CRUD.
 */
@Repository
public interface PaymentRepository extends RepositoryPort<Payment, String> {
    // Aqui você pode colocar métodos específicos do domínio se precisar
    // ex: findByStatus(PaymentStatus status)
}
